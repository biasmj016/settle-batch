package com.batch.settlement.settings

import com.batch.settlement.domain.PartnerTransactions
import com.batch.settlement.domain.Transactions
import com.batch.settlement.repository.*
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

/*
테스트 진행 방법
1. all_matched_data: 내부 시스템의 거래 데이터와 파트너 거래 데이터가 모두 일치하는 케이스를 테스트 할 때 실행
2. transaction_unmatched_data: 내부 시스템 상에는 거래내역이 있으나 파트너 거래데이터가 없는 경우를 테스트 할 때 실행
3. partner_transaction_unmatched_data: 내부 시스템 상에는 거래내역이 없으나 파트너 거래내역은 있는 경우를 테스트 할 때 실행
4. all_test : 내부 시스템 상에는 거래내역이 있으나 파트너 거래데이터가 없는 경우 &  내부 시스템 상에는 거래내역이 없으나 파트너 거래내역은 있는 경우 두 가지 케이스를 동시에 테스트 할 때 실행
*/


@TestConfiguration
class TransactionsSettings @Autowired constructor(
    private val transactionsRepository: TransactionsRepository,
    private val partnerTransactionsRepository: PartnerTransactionsRepository,
    private val matchedTransactionsRepository: MatchedTransactionsRepository,
    private val cancellationTransactionsRepository: CancellationTransactionsRepository,
    private val suspiciousTransactionsRepository: SuspiciousTransactionsRepository
) {

    @PersistenceContext
    lateinit var entityManager: EntityManager

    var transactionRows: Int = 10000
    var percent: Int = 1

    private val transactionList = mutableListOf<Transactions>()
    private val partnerTransactionList = mutableListOf<PartnerTransactions>()

    companion object {
        private val FEE = BigDecimal(1000)
        private val PARTNER_FEE = BigDecimal(500)
        private val ADD = BigDecimal(1000)
    }

    fun cleanDb() {
        transactionsRepository.deleteAllTransactions()
        partnerTransactionsRepository.deleteAllTransactions()
        matchedTransactionsRepository.deleteAllTransactions()
        cancellationTransactionsRepository.deleteAllTransactions()
        suspiciousTransactionsRepository.deleteAllTransactions()
    }

    private fun generateData(count: Int, transform: (Int, String) -> Pair<String, String>) {
        transactionList.clear()
        partnerTransactionList.clear()
        val base = LocalDateTime.now()
        for (i in 0 until count) {
            val defaultId = UUID.randomUUID().toString()
            val (transactionId, partnerTransactionId) = transform(i, defaultId)
            val partnerTransactionTime = base.plusSeconds(i.toLong())
            val transactionTime = partnerTransactionTime.minusNanos(1_000_000)
            val amount = BigDecimal(Random.nextInt(1, 1000) * 1000)
            transactionList.add(Transactions(transactionId = transactionId, amount = amount, fee = FEE, transactionDate = transactionTime))
            partnerTransactionList.add(PartnerTransactions(transactionId = partnerTransactionId, amount = amount + ADD, fee = PARTNER_FEE, transactionDate = partnerTransactionTime)
            )
        }
    }

    // 내부와 파트너 모두에 동일한 데이터 생성
    fun all_matched_data() = generateData(transactionRows) { _, id -> Pair(id, id) }

    // 내부 데이터는 모두 생성하되, partner 데이터는 처음 'unmatchedCount' 건은 추가하지 않음
    fun transaction_unmatched_data() {
        transactionList.clear(); partnerTransactionList.clear()
        val unmatched = transactionRows * percent / 100
        val base = LocalDateTime.now()
        for (i in 0 until transactionRows) {
            val id = UUID.randomUUID().toString()
            val partnerTransactionTime = base.plusSeconds(i.toLong())
            val transactionTime = partnerTransactionTime.minusNanos(1_000_000)
            val amount = BigDecimal(Random.nextInt(1, 1000) * 1000)
            transactionList.add(Transactions(transactionId = id, amount = amount, fee = FEE, transactionDate = transactionTime))
            if (i >= unmatched) {
                partnerTransactionList.add(
                    PartnerTransactions(
                        transactionId = id,
                        amount = amount + ADD,
                        fee = PARTNER_FEE,
                        transactionDate = partnerTransactionTime
                    )
                )
            }
        }
    }

    // partner 데이터는 모두 생성하되, 내부 데이터는 처음 'unmatchedCount' 건은 추가하지 않음
    fun partner_transaction_unmatched_data() {
        transactionList.clear(); partnerTransactionList.clear()
        val unmatched = transactionRows * percent / 100
        val base = LocalDateTime.now()
        for (i in 0 until transactionRows) {
            val id = UUID.randomUUID().toString()
            val partnerTransactionTime = base.plusSeconds(i.toLong())
            val transactionTime = partnerTransactionTime.minusNanos(1_000_000)
            val amount = BigDecimal(Random.nextInt(1, 1000) * 1000)
            if (i >= unmatched) {
                transactionList.add(Transactions(transactionId = id, amount = amount, fee = FEE, transactionDate = transactionTime))
            }
            partnerTransactionList.add(
                PartnerTransactions(
                    transactionId = id,
                    amount = amount + ADD,
                    fee = PARTNER_FEE,
                    transactionDate = partnerTransactionTime
                )
            )
        }
    }

    // 내부 전용, 파트너 전용, 매칭 데이터를 구분하여 생성
    fun all_test() {
        val transOnly = transactionRows * percent / 100
        val partnerOnly = transactionRows * percent / 100
        val matched = transactionRows - transOnly - partnerOnly
        transactionList.clear()
        partnerTransactionList.clear()
        val base = LocalDateTime.now()

        for (i in 0 until matched) {
            val id = UUID.randomUUID().toString()
            val amount = BigDecimal(Random.nextInt(1, 1000) * 1000)
            transactionList.add(
                Transactions(
                    transactionId = id,
                    amount = amount,
                    fee = FEE,
                    transactionDate = base.plusSeconds(i.toLong()).minusNanos(1_000_000)
                )
            )
            partnerTransactionList.add(
                PartnerTransactions(
                    transactionId = id,
                    amount = amount + ADD,
                    fee = PARTNER_FEE,
                    transactionDate = base.plusSeconds(i.toLong())
                )
            )
        }

        // 내부 전용 거래: partner 데이터 미포함
        for (i in 0 until transOnly) {
            val id = UUID.randomUUID().toString()
            transactionList.add(
                Transactions(
                    transactionId = id,
                    amount = BigDecimal(Random.nextInt(1, 1000) * 1000),
                    fee = FEE,
                    transactionDate = base.plusSeconds((i + matched).toLong())
                )
            )
        }

        // 파트너 전용 거래: 내부 데이터 미포함
        for (i in 0 until partnerOnly) {
            val id = UUID.randomUUID().toString()
            partnerTransactionList.add(
                PartnerTransactions(
                    transactionId = id,
                    amount = BigDecimal(Random.nextInt(1, 1000) * 1000) + ADD,
                    fee = PARTNER_FEE,
                    transactionDate = base.plusSeconds((i + matched + transOnly).toLong())
                )
            )
        }
    }

    @Transactional
    fun saveData(batchSize: Int = 300) {
        transactionList.forEachIndexed { index, transaction ->
            entityManager.persist(transaction)
            if ((index + 1) % batchSize == 0) {
                entityManager.flush()
                entityManager.clear()
            }
        }
        entityManager.flush()
        entityManager.clear()

        partnerTransactionList.forEachIndexed { index, partnerTransaction ->
            entityManager.persist(partnerTransaction)
            if ((index + 1) % batchSize == 0) {
                entityManager.flush()
                entityManager.clear()
            }
        }
        entityManager.flush()
        entityManager.clear()
    }
}