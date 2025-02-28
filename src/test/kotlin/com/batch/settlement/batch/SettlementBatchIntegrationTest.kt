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
        private val EXPECTED_LOSS = BigDecimal(1000)
    }

    @BeforeEach
    fun setup() {
        transactionsSettings.cleanDb()
    }

    private fun launchJob(job: Job): JobExecution {
        val jobParameters = JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters()
        return jobLauncher.run(job, jobParameters)
    }

    @Test
    fun `매칭 데이터`() {
        transactionsSettings.all_matched_data()
        transactionsSettings.saveData()

        val jobExecution = launchJob(settlementJob)

        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        val matchedResults = matchedRepository.findAll()
        assertThat(matchedResults).hasSize(transactionsSettings.transactionRows)
        matchedResults.forEach { matched ->
            assertThat(matched.netProfit.compareTo(EXPECTED_NET_PROFIT)).isEqualTo(0)
            assertThat(matched.grossProfit.compareTo(EXPECTED_GROSS_PROFIT)).isEqualTo(0)
        }
        assertThat(cancellationRepository.findAll()).isEmpty()
        assertThat(suspiciousRepository.findAll()).isEmpty()
    }

    @Test
    fun `취소 거래`() {
        transactionsSettings.transaction_unmatched_data()
        transactionsSettings.saveData()

        val jobExecution = launchJob(settlementJob)

        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        val cancellationResults = cancellationRepository.findAll()
        val expectedCancellation = transactionsSettings.transactionRows * transactionsSettings.percent / 100
        assertThat(cancellationResults).hasSize(expectedCancellation)
        assertThat(matchedRepository.findAll()).hasSize(transactionsSettings.transactionRows - expectedCancellation)
        assertThat(suspiciousRepository.findAll()).isEmpty()
    }

    @Test
    fun `이상 거래`() {
        transactionsSettings.partner_transaction_unmatched_data()
        transactionsSettings.saveData()

        val jobExecution = launchJob(settlementJob)

        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        val suspiciousResults = suspiciousRepository.findAll()
        val expectedSuspicious = transactionsSettings.transactionRows * transactionsSettings.percent / 100
        assertThat(suspiciousResults).hasSize(expectedSuspicious)
        assertThat(matchedRepository.findAll()).hasSize(transactionsSettings.transactionRows - expectedSuspicious)
        assertThat(cancellationRepository.findAll()).isEmpty()
    }

    @Test
    fun `모든 케이스`() {
        transactionsSettings.all_test()
        transactionsSettings.saveData()

        val jobExecution = launchJob(settlementJob)

        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)

        val matchedCount = matchedRepository.count()
        val expectedMatched = transactionsSettings.transactionRows * (100 - 2 * transactionsSettings.percent) / 100
        assertThat(matchedCount.compareTo(expectedMatched)).isEqualTo(0)

        val cancellationCount = cancellationRepository.count()
        val expectedCancellation = transactionsSettings.transactionRows * transactionsSettings.percent / 100
        assertThat(cancellationCount.compareTo(expectedCancellation)).isEqualTo(0)

        val suspiciousCount = suspiciousRepository.count()
        val expectedSuspicious = transactionsSettings.transactionRows * transactionsSettings.percent / 100
        assertThat(suspiciousCount.compareTo(expectedSuspicious)).isEqualTo(0)
    }
}
