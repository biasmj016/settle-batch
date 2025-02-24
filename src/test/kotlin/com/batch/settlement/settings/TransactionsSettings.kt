package com.batch.settlement.settings

import com.batch.settlement.domain.PartnerTransactions
import com.batch.settlement.domain.Transactions
import com.batch.settlement.repository.PartnerTransactionsRepository
import com.batch.settlement.repository.TransactionsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
    private val partnerTransactionsRepository: PartnerTransactionsRepository
) {
    var transactionRows: Int = 3000
    var percent: Int = 1

    private val transactionList = mutableListOf<Transactions>()
    private val partnerTransactionList = mutableListOf<PartnerTransactions>()

    fun cleanDb() {
        transactionsRepository.deleteAll()
        partnerTransactionsRepository.deleteAll()
    }

    fun getTransactionList(): List<Transactions> = transactionList
    fun getPartnerTransactionList(): List<PartnerTransactions> = partnerTransactionList

    private fun generateData(
        count: Int,
        idFunc: (i: Int, defaultId: String) -> Pair<String, String>
    ) {
        transactionList.clear()
        partnerTransactionList.clear()

        for (i in 0 until count) {
            val defaultId = UUID.randomUUID().toString()
            val (transactionId, partnerTransactionId) = idFunc(i, defaultId)
            val partnerTransactionDate = LocalDateTime.now().plusSeconds(i.toLong())
            val transactionDate = partnerTransactionDate.minusNanos(1_000_000)
            val amount = BigDecimal(Random.nextInt(1, 1000) * 1000)

            transactionList.add(
                Transactions(
                    transactionId = transactionId,
                    amount = amount,
                    fee = BigDecimal(1000),
                    transactionDate = transactionDate
                )
            )
            partnerTransactionList.add(
                PartnerTransactions(
                    transactionId = partnerTransactionId,
                    amount = amount + BigDecimal(1000),
                    fee = BigDecimal(500),
                    transactionDate = partnerTransactionDate
                )
            )
        }
    }

    fun all_matched_data() {
        generateData(transactionRows) { _, defaultId -> Pair(defaultId, defaultId) }
    }

    fun transaction_unmatched_data() {
        generateData(transactionRows) { _, defaultId -> Pair(defaultId, defaultId) }
        val unmatchedCount = (transactionRows * percent / 100)
        partnerTransactionList.drop(unmatchedCount)
    }

    fun partner_transaction_unmatched_data() {
        generateData(transactionRows) { _, defaultId -> Pair(defaultId, defaultId) }
        val unmatchedCount = (transactionRows * percent / 100)
        transactionList.drop(unmatchedCount)
    }

    fun all_test() {
        val transactionOnly = transactionRows * percent / 100
        val partnerTransactionOnly = transactionRows * percent / 100
        generateData(transactionRows) { i, defaultId ->
            when {
                i < transactionOnly -> Pair(defaultId, UUID.randomUUID().toString())
                i < transactionOnly + partnerTransactionOnly -> Pair(UUID.randomUUID().toString(), defaultId)
                else -> Pair(defaultId, defaultId)
            }
        }
    }

    fun saveData() {
        transactionsRepository.saveAll(transactionList)
        partnerTransactionsRepository.saveAll(partnerTransactionList)
    }
}