package com.batch.settlement.settings

import com.batch.settlement.domain.PartnerTransactions
import com.batch.settlement.domain.Transactions
import com.batch.settlement.repository.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
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
    var transactionRows: Int = 3000
    var percent: Int = 1

    private var transactionList = mutableListOf<Transactions>()
    private var partnerTransactionList = mutableListOf<PartnerTransactions>()

    companion object {
        private val TRANSACTION_FEE = BigDecimal(1000)
        private val PARTNER_FEE = BigDecimal(500)
        private val AMOUNT_ADDITION = BigDecimal(1000)
    }

    fun cleanDb() {
        listOf(
            transactionsRepository,
            partnerTransactionsRepository,
            matchedTransactionsRepository,
            cancellationTransactionsRepository,
            suspiciousTransactionsRepository
        ).forEach { it.deleteAll() }
    }

    private fun generateData(
        count: Int,
        idFunc: (index: Int, defaultId: String) -> Pair<String, String>
    ) {
        transactionList.clear()
        partnerTransactionList.clear()
        val baseTime = LocalDateTime.now()

        for (i in 0 until count) {
            val defaultId = UUID.randomUUID().toString()
            val (transId, partnerId) = idFunc(i, defaultId)
            val partnerTime = baseTime.plusSeconds(i.toLong())
            val transTime = partnerTime.minusNanos(1_000_000)
            val amount = BigDecimal(Random.nextInt(1, 1000) * 1000)

            transactionList.add(
                Transactions(
                    transactionId = transId,
                    amount = amount,
                    fee = TRANSACTION_FEE,
                    transactionDate = transTime
                )
            )
            partnerTransactionList.add(
                PartnerTransactions(
                    transactionId = partnerId,
                    amount = amount + AMOUNT_ADDITION,
                    fee = PARTNER_FEE,
                    transactionDate = partnerTime
                )
            )
        }
    }

    // 내부와 파트너 모두에 동일한 데이터 생성
    fun all_matched_data() {
        generateData(transactionRows) { _, defaultId -> Pair(defaultId, defaultId) }
    }

    // 내부 데이터는 모두 생성하되, partner 데이터는 처음 'unmatchedCount' 건은 추가하지 않음
    fun transaction_unmatched_data() {
        transactionList.clear()
        partnerTransactionList.clear()
        val unmatchedCount = transactionRows * percent / 100
        val baseTime = LocalDateTime.now()

        for (i in 0 until transactionRows) {
            val id = UUID.randomUUID().toString()
            val partnerTime = baseTime.plusSeconds(i.toLong())
            val transTime = partnerTime.minusNanos(1_000_000)
            val amount = BigDecimal(Random.nextInt(1, 1000) * 1000)

            transactionList.add(
                Transactions(
                    transactionId = id,
                    amount = amount,
                    fee = TRANSACTION_FEE,
                    transactionDate = transTime
                )
            )
            if (i >= unmatchedCount) {
                partnerTransactionList.add(
                    PartnerTransactions(
                        transactionId = id,
                        amount = amount + AMOUNT_ADDITION,
                        fee = PARTNER_FEE,
                        transactionDate = partnerTime
                    )
                )
            }
        }
    }

    // partner 데이터는 모두 생성하되, 내부 데이터는 처음 'unmatchedCount' 건은 추가하지 않음
    fun partner_transaction_unmatched_data() {
        transactionList.clear()
        partnerTransactionList.clear()
        val unmatchedCount = transactionRows * percent / 100
        val baseTime = LocalDateTime.now()

        for (i in 0 until transactionRows) {
            val id = UUID.randomUUID().toString()
            val partnerTime = baseTime.plusSeconds(i.toLong())
            val transTime = partnerTime.minusNanos(1_000_000)
            val amount = BigDecimal(Random.nextInt(1, 1000) * 1000)

            if (i >= unmatchedCount) {
                transactionList.add(
                    Transactions(
                        transactionId = id,
                        amount = amount,
                        fee = TRANSACTION_FEE,
                        transactionDate = transTime
                    )
                )
            }
            partnerTransactionList.add(
                PartnerTransactions(
                    transactionId = id,
                    amount = amount + AMOUNT_ADDITION,
                    fee = PARTNER_FEE,
                    transactionDate = partnerTime
                )
            )
        }
    }

    // 내부 전용, 파트너 전용, 매칭 데이터를 구분하여 생성
    fun all_test() {
        val transOnlyCount = transactionRows * percent / 100
        val partnerOnlyCount = transactionRows * percent / 100
        val matchedCount = transactionRows - transOnlyCount - partnerOnlyCount

        transactionList.clear()
        partnerTransactionList.clear()
        val baseTime = LocalDateTime.now()

        // 매칭 거래: 내부와 파트너 모두 저장
        (0 until matchedCount).forEach { i ->
            val id = UUID.randomUUID().toString()
            val amount = BigDecimal(Random.nextInt(1, 1000) * 1000)
            val transTime = baseTime.plusSeconds(i.toLong()).minusNanos(1_000_000)
            val partnerTime = baseTime.plusSeconds(i.toLong())

            transactionList.add(
                Transactions(
                    transactionId = id,
                    amount = amount,
                    fee = TRANSACTION_FEE,
                    transactionDate = transTime
                )
            )
            partnerTransactionList.add(
                PartnerTransactions(
                    transactionId = id,
                    amount = amount + AMOUNT_ADDITION,
                    fee = PARTNER_FEE,
                    transactionDate = partnerTime
                )
            )
        }

        // 내부 전용 거래: partner 데이터 미포함
        (0 until transOnlyCount).forEach { i ->
            val id = UUID.randomUUID().toString()
            val amount = BigDecimal(Random.nextInt(1, 1000) * 1000)
            val transTime = baseTime.plusSeconds((i + matchedCount).toLong())

            transactionList.add(
                Transactions(
                    transactionId = id,
                    amount = amount,
                    fee = TRANSACTION_FEE,
                    transactionDate = transTime
                )
            )
        }

        // 파트너 전용 거래: 내부 데이터 미포함
        (0 until partnerOnlyCount).forEach { i ->
            val id = UUID.randomUUID().toString()
            val amount = BigDecimal(Random.nextInt(1, 1000) * 1000)
            val partnerTime = baseTime.plusSeconds((i + matchedCount + transOnlyCount).toLong())

            partnerTransactionList.add(
                PartnerTransactions(
                    transactionId = id,
                    amount = amount + AMOUNT_ADDITION,
                    fee = PARTNER_FEE,
                    transactionDate = partnerTime
                )
            )
        }
    }

    fun saveData() {
        transactionsRepository.saveAll(transactionList)
        partnerTransactionsRepository.saveAll(partnerTransactionList)
    }
}