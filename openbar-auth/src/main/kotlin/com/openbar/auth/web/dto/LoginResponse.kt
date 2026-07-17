package com.openbar.auth.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "Resposta do login com token JWT")
data class LoginResponse(
    @field:Schema(description = "Token JWT de autenticação", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,

    @field:Schema(description = "Refresh token para renovação da sessão")
    val refreshToken: UUID,

    @field:Schema(description = "Tempo de expiração do access token em segundos", example = "3600")
    val expiresIn: Long,

    @field:Schema(description = "Data de expiração do refresh token")
    val refreshExpiresAt: Instant
)
