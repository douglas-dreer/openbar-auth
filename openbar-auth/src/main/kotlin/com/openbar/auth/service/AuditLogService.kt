package com.openbar.auth.service

import com.openbar.auth.domain.model.AuditLog
import com.openbar.auth.domain.repository.AuditLogRepository
import com.openbar.auth.web.dto.AuditLogResponse
import com.openbar.auth.web.dto.toResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuditLogService(
    private val auditLogRepository: AuditLogRepository
) {

    fun record(
        userId: UUID? = null,
        username: String? = null,
        action: String,
        details: String? = null,
        endpoint: String,
        method: String,
        ipAddress: String? = null,
        success: Boolean = true
    ): AuditLog {
        return auditLogRepository.save(
            AuditLog(
                userId = userId,
                username = username,
                action = action,
                details = details,
                endpoint = endpoint,
                method = method,
                ipAddress = ipAddress,
                success = success
            )
        )
    }

    fun findAll(pageable: Pageable): Page<AuditLogResponse> {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable).map { it.toResponse() }
    }

    fun findByUserId(userId: UUID, pageable: Pageable): Page<AuditLogResponse> {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable).map { it.toResponse() }
    }

    fun findByAction(action: String, pageable: Pageable): Page<AuditLogResponse> {
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable).map { it.toResponse() }
    }
}
