package com.openbar.auth.domain.repository

import com.openbar.auth.domain.model.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.Optional
import java.util.UUID

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {

    fun findByToken(token: UUID): Optional<RefreshToken>

    fun findByTokenAndRevokedFalse(token: UUID): Optional<RefreshToken>

    fun deleteByExpiresAtBefore(now: Instant)

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    fun revokeAllByUserId(@Param("userId") userId: UUID)
}
