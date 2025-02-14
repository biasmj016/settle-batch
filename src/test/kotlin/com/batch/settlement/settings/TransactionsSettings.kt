package com.batch.settlement.settings

import com.batch.settlement.domain.PartnerTransactions
import com.batch.settlement.domain.Transactions
import com.batch.settlement.repository.PartnerTransactionsRepository
import com.batch.settlement.repository.TransactionsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
- 테스트 실행시 전에 저장했던 내역은 모두 지워지고 실행한 테스트로 생성해낸 데이터로 다시 저장됨
*/


@SpringBootTest
class TransactionsSettings {

    @Autowired
    private lateinit var transactionsRepository: TransactionsRepository

    @Autowired
    private lateinit var partnerTransactionsRepository: PartnerTransactionsRepository

    private val transactionRows = 3000
    private val percent = 1

    @BeforeEach
    fun cleanDb() {
        transactionsRepository.deleteAll()
        partnerTransactionsRepository.deleteAll()
    }

    private fun generateData(
        count: Int,
        idFunc: (i: Int, defaultId: String) -> Pair<String, String>
    ): Pair<List<Transactions>, List<PartnerTransactions>> {
        val transactionList = mutableListOf<Transactions>()
        val partnerTransactionList = mutableListOf<PartnerTransactions>()

        for (i in 0 until count) {
            val defaultId = UUID.randomUUID().toString()
            val (transactionId, partnerTransactionId) = idFunc(i, defaultId)
            val partnerTransactionDate = LocalDateTime.now().plusSeconds(i.toLong())
            val transactionDate = partnerTransactionDate.minusNanos(1_000_000)// 파트너 사로 요청이 들어가는 시간보다 1ms 의 차가 있다고 설정
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
        return Pair(transactionList, partnerTransactionList)
    }

    @Test
    fun all_matched_data() {
        // 모든 거래가 양쪽에 동일한 거래번호로 저장됨
        val (transactionList, partnerTransactionList) = generateData(transactionRows) { _, defaultId -> Pair(defaultId, defaultId) }
        transactionsRepository.saveAll(transactionList)
        partnerTransactionsRepository.saveAll(partnerTransactionList)
    }

    @Test
    fun transaction_unmatched_data() {
        // 내부 시스템 거래는 있으나, 파트너 거래는 일부 다른 거래번호로 저장 (불일치)
        val unmatchedCount = (transactionRows * percent / 100).toInt()
        val (transactionList, fullPartnerList) = generateData(transactionRows) { _, defaultId -> Pair(defaultId, defaultId) }
        val partnerTransactionList = fullPartnerList.drop(unmatchedCount)

        transactionsRepository.saveAll(transactionList)
        partnerTransactionsRepository.saveAll(partnerTransactionList)
    }

    @Test
    fun partner_transaction_unmatched_data() {
        // 파트너 거래는 있으나, 내부 시스템 거래는 일부 다른 거래번호로 저장 (불일치)
        val unmatchedCount = (transactionRows * percent / 100).toInt()
        val (fullTransactionList, partnerTransactionList) = generateData(transactionRows) { _, defaultId -> Pair(defaultId, defaultId) }
        val transactionList = fullTransactionList.drop(unmatchedCount)

        transactionsRepository.saveAll(transactionList)
        partnerTransactionsRepository.saveAll(partnerTransactionList)
    }

    @Test
    fun all_test() {
        // Case 1: 내부 거래만 존재, Case 2: 파트너 거래만 존재, Case 3: 매칭되는 거래
        val transactionOnly = transactionRows * percent / 100
        val partnerTransactionOnly = transactionRows * percent / 100
        val (transactionList, partnerTransactionList) = generateData(transactionRows) { i, defaultId ->
            when {
                i < transactionOnly -> Pair(defaultId, UUID.randomUUID().toString())
                i < transactionOnly + partnerTransactionOnly -> Pair(UUID.randomUUID().toString(), defaultId)
                else -> Pair(defaultId, defaultId)
            }
        }
        transactionsRepository.saveAll(transactionList)
        partnerTransactionsRepository.saveAll(partnerTransactionList)
    }
}
