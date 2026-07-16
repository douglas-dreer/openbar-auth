package com.openbar.auth.web.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Resposta do login com token JWT")
data class LoginResponse(
    @field:Schema(description = "Token JWT de autenticação", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,

    @field:Schema(description = "Tempo de expiração em segundos", example = "3600")
    val expiresIn: Long
)
