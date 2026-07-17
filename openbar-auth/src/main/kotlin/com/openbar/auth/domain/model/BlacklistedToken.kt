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
@Table(name = "blacklisted_tokens")
data class BlacklistedToken(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val jti: String,

    @Column(nullable = false)
    val expiresAt: Instant,

    @Column(nullable = false)
    val blacklistedAt: Instant = Instant.now()
)
