package com.openbar.auth.web.dto

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class RefreshTokenRequest(
    @field:NotNull(message = "Refresh token is required")
    val refreshToken: UUID
)
