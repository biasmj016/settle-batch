package com.batch.settlement.repository

import com.batch.settlement.domain.AuditLog
import org.springframework.data.jpa.repository.JpaRepository

interface AuditLogRepository : JpaRepository<AuditLog, Long>