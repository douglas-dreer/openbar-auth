package com.openbar.auth.service

import com.openbar.auth.domain.model.RefreshToken
import com.openbar.auth.domain.model.User
import com.openbar.auth.domain.repository.RefreshTokenRepository
import com.openbar.auth.security.JwtTokenProvider
import com.openbar.auth.web.dto.RefreshTokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Value("\${jwt.refresh-expiration:604800000}")
    private val refreshExpirationMs: Long = 604800000 // 7 days

    @Transactional
    fun createRefreshToken(user: User): RefreshToken {
        val expiresAt = Instant.now().plusMillis(refreshExpirationMs)

        return refreshTokenRepository.save(
            RefreshToken(
                user = user,
                expiresAt = expiresAt
            )
        )
    }

    @Transactional
    fun refresh(refreshTokenValue: UUID): RefreshTokenResponse {
        val refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue)
            .orElseThrow { IllegalArgumentException("Invalid or revoked refresh token") }

        if (refreshToken.expiresAt.isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken)
            throw IllegalArgumentException("Refresh token has expired")
        }

        val user = refreshToken.user

        val newAccessToken = jwtTokenProvider.generateToken(user)
        val newRefreshToken = createRefreshToken(user)

        refreshTokenRepository.delete(refreshToken)

        return RefreshTokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken.token!!,
            expiresIn = jwtTokenProvider.getExpirationMs() / 1000,
            refreshExpiresAt = newRefreshToken.expiresAt
        )
    }

    @Transactional
    fun revokeRefreshToken(refreshTokenValue: UUID) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent { token ->
            refreshTokenRepository.delete(token)
        }
    }

    @Transactional
    fun revokeAllUserTokens(userId: UUID) {
        refreshTokenRepository.revokeAllByUserId(userId)
    }

    @Transactional
    fun cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now())
    }
}
