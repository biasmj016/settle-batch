package com.batch.settlement.batch

import com.batch.settlement.batch.result.SettlementResult
import com.batch.settlement.domain.Transactions
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime

@Configuration
class SettlementJobConfig(
    private val partnerTransactionCacheTasklet: PartnerTransactionCacheTasklet,
    private val transactionsMatchingProcessor: TransactionsMatchingProcessor,
    private val matchingResultWriter: MatchingResultWriter,
    private val suspiciousTransactionTasklet: SuspiciousTransactionTasklet,
    private val settlementStatisticsTasklet: SettlementStatisticsTasklet,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {

    @Bean
    fun transactionsReader(entityManagerFactory: EntityManagerFactory): JpaPagingItemReader<Transactions> {
        val reader = JpaPagingItemReader<Transactions>()
        val start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)
        reader.setQueryString("SELECT t FROM Transactions t WHERE t.transactionDate BETWEEN :start AND :end")
        reader.setParameterValues(mapOf("start" to start, "end" to start.plusDays(1)))
        reader.setEntityManagerFactory(entityManagerFactory)
        reader.pageSize = 10
        return reader
    }

    @Bean
    fun taskExecutor(): TaskExecutor {
        val taskExecutor = SimpleAsyncTaskExecutor("batch_task_executor")
        taskExecutor.concurrencyLimit = 4
        return taskExecutor
    }

    @Bean
    fun transactionsStep(reader: JpaPagingItemReader<Transactions>): Step {
        return StepBuilder("transactionsStep", jobRepository)
            .chunk<Transactions, SettlementResult>(10, transactionManager)
            .reader(reader)
            .processor(transactionsMatchingProcessor)
            .writer(matchingResultWriter)
            .taskExecutor(taskExecutor())
            .build()
    }

    @Bean
    fun partnerCacheStep(): Step {
        return StepBuilder("partnerCacheStep", jobRepository)
            .tasklet(partnerTransactionCacheTasklet, transactionManager)
            .build()
    }

    @Bean
    fun suspiciousStep(): Step {
        return StepBuilder("suspiciousStep", jobRepository)
            .tasklet(suspiciousTransactionTasklet, transactionManager)
            .build()
    }

    @Bean
    fun statisticsStep(): Step {
        return StepBuilder("statisticsStep", jobRepository)
            .tasklet(settlementStatisticsTasklet, transactionManager)
            .build()
    }

    @Bean
    fun settlementJob(reader: JpaPagingItemReader<Transactions>): Job {
        return JobBuilder("settlementJob", jobRepository)
            .start(partnerCacheStep())
            .next(transactionsStep(reader))
            .next(suspiciousStep())
            .next(statisticsStep())
            .build()
    }
}