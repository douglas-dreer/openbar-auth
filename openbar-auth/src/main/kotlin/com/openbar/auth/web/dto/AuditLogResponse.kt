package com.openbar.auth.web.dto

import com.openbar.auth.domain.model.AuditLog
import java.time.Instant
import java.util.UUID

data class AuditLogResponse(
    val id: UUID,
    val userId: UUID?,
    val username: String?,
    val action: String,
    val details: String?,
    val endpoint: String,
    val method: String,
    val ipAddress: String?,
    val success: Boolean,
    val timestamp: Instant
)

fun AuditLog.toResponse() = AuditLogResponse(
    id = id!!,
    userId = userId,
    username = username,
    action = action,
    details = details,
    endpoint = endpoint,
    method = method,
    ipAddress = ipAddress,
    success = success,
    timestamp = timestamp
)
