package com.openbar.auth.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audit_logs")
data class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id")
    val userId: UUID? = null,

    @Column(nullable = false)
    val username: String? = null,

    @Column(nullable = false)
    val action: String,

    @Column(columnDefinition = "TEXT")
    val details: String? = null,

    @Column(nullable = false)
    val endpoint: String,

    @Column(nullable = false)
    val method: String,

    @Column(nullable = false)
    val ipAddress: String? = null,

    @Column(nullable = false)
    val success: Boolean = true,

    @Column(nullable = false)
    val timestamp: Instant = Instant.now()
)
