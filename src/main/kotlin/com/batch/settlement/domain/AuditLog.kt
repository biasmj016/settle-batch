package com.batch.settlement.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "audit_logs")
data class AuditLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val event: String = "",
    val receivedAt: LocalDateTime = LocalDateTime.now()
)