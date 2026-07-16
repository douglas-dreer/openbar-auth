package com.openbar.auth.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Dados de login")
data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    @field:Schema(description = "Email do usuário", example = "admin@openbar.com")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Schema(description = "Senha do usuário", example = "123456")
    val password: String
)
