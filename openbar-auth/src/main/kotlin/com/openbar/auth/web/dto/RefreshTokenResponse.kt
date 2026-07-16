package com.openbar.auth.web.dto

import java.time.Instant
import java.util.UUID

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: UUID,
    val expiresIn: Long,
    val refreshExpiresAt: Instant
)
