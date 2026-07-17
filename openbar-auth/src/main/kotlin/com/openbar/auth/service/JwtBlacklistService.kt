package com.openbar.auth.service

import com.openbar.auth.domain.model.BlacklistedToken
import com.openbar.auth.domain.repository.BlacklistedTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class JwtBlacklistService(
    private val blacklistedTokenRepository: BlacklistedTokenRepository
) {

    @Transactional
    fun blacklistToken(jti: String, expiresAt: Instant) {
        if (!blacklistedTokenRepository.existsByJti(jti)) {
            blacklistedTokenRepository.save(
                BlacklistedToken(
                    jti = jti,
                    expiresAt = expiresAt
                )
            )
        }
    }

    fun isTokenBlacklisted(jti: String): Boolean {
        return blacklistedTokenRepository.existsByJti(jti)
    }

    @Transactional
    fun cleanupExpiredTokens() {
        blacklistedTokenRepository.deleteByExpiresAtBefore(Instant.now())
    }
}
