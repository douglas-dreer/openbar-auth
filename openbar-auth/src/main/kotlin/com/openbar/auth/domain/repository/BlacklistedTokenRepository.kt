package com.openbar.auth.domain.repository

import com.openbar.auth.domain.model.BlacklistedToken
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.Optional
import java.util.UUID

interface BlacklistedTokenRepository : JpaRepository<BlacklistedToken, UUID> {

    fun existsByJti(jti: String): Boolean

    fun deleteByExpiresAtBefore(now: Instant)
}
