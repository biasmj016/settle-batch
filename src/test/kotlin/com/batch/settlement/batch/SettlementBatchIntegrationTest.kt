package com.batch.settlement.batch

import com.batch.settlement.repository.CancellationTransactionsRepository
import com.batch.settlement.repository.MatchedTransactionsRepository
import com.batch.settlement.repository.SuspiciousTransactionsRepository
import com.batch.settlement.settings.TransactionsSettings
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.math.BigDecimal

@SpringBootTest
@Import(TransactionsSettings::class)
class SettlementBatchIntegrationTest {

    @Autowired
    lateinit var jobLauncher: JobLauncher

    @Autowired
    lateinit var settlementJob: Job

    @Autowired
    lateinit var matchedRepository: MatchedTransactionsRepository

    @Autowired
    lateinit var cancellationRepository: CancellationTransactionsRepository

    @Autowired
    lateinit var suspiciousRepository: SuspiciousTransactionsRepository

    @Autowired
    lateinit var transactionsSettings: TransactionsSettings

    companion object {
        private val EXPECTED_NET_PROFIT = BigDecimal(500)
        private val EXPECTED_GROSS_PROFIT = BigDecimal(1000)
    }

    @BeforeEach
    fun setup() {
        transactionsSettings.cleanDb()
    }

    private fun runAndAssertJob(): JobExecution {
        val jobParameters = JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters()
        val jobExecution = jobLauncher.run(settlementJob, jobParameters)
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        return jobExecution
    }

    @Test
    fun `매칭 데이터`() {
        transactionsSettings.all_matched_data()
        transactionsSettings.saveData()

        runAndAssertJob()

        val matchedResults = matchedRepository.findAll()
        assertThat(matchedResults).hasSize(transactionsSettings.transactionRows)
        matchedResults.forEach { matched ->
            assertThat(matched.netProfit).isEqualByComparingTo(EXPECTED_NET_PROFIT)
            assertThat(matched.grossProfit).isEqualByComparingTo(EXPECTED_GROSS_PROFIT)
        }
        assertThat(cancellationRepository.findAll()).isEmpty()
        assertThat(suspiciousRepository.findAll()).isEmpty()
    }

    @Test
    fun `취소 거래`() {
        transactionsSettings.transaction_unmatched_data()
        transactionsSettings.saveData()

        runAndAssertJob()

        val expectedCancellation = transactionsSettings.transactionRows * transactionsSettings.percent / 100
        val cancellationResults = cancellationRepository.findAll()
        assertThat(cancellationResults).hasSize(expectedCancellation)
        val expectedMatched = transactionsSettings.transactionRows - expectedCancellation
        assertThat(matchedRepository.findAll()).hasSize(expectedMatched)
        assertThat(suspiciousRepository.findAll()).isEmpty()
    }

    @Test
    fun `이상 거래`() {
        transactionsSettings.partner_transaction_unmatched_data()
        transactionsSettings.saveData()

        runAndAssertJob()

        val expectedSuspicious = transactionsSettings.transactionRows * transactionsSettings.percent / 100
        val suspiciousResults = suspiciousRepository.findAll()
        assertThat(suspiciousResults).hasSize(expectedSuspicious)
        val expectedMatched = transactionsSettings.transactionRows - expectedSuspicious
        assertThat(matchedRepository.findAll()).hasSize(expectedMatched)
        assertThat(cancellationRepository.findAll()).isEmpty()
    }

    @Test
    fun `모든 케이스`() {
        transactionsSettings.all_test()
        transactionsSettings.saveData()

        runAndAssertJob()

        val expectedMatched = transactionsSettings.transactionRows * (100 - 2 * transactionsSettings.percent) / 100
        assertThat(matchedRepository.count()).isEqualTo(expectedMatched.toLong())

        val expectedCancellation = transactionsSettings.transactionRows * transactionsSettings.percent / 100
        assertThat(cancellationRepository.count()).isEqualTo(expectedCancellation.toLong())

        val expectedSuspicious = transactionsSettings.transactionRows * transactionsSettings.percent / 100
        assertThat(suspiciousRepository.count()).isEqualTo(expectedSuspicious.toLong())
    }
}