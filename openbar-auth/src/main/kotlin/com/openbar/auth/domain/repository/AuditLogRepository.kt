package com.openbar.auth.domain.repository

import com.openbar.auth.domain.model.AuditLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AuditLogRepository : JpaRepository<AuditLog, UUID> {

    fun findByUserIdOrderByTimestampDesc(userId: UUID, pageable: Pageable): Page<AuditLog>

    fun findAllByOrderByTimestampDesc(pageable: Pageable): Page<AuditLog>

    fun findByActionOrderByTimestampDesc(action: String, pageable: Pageable): Page<AuditLog>
}
