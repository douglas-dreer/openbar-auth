package com.openbar.auth.web.dto

import com.openbar.auth.domain.model.UserRole
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "Dados para criação de usuário")
data class CreateUserRequest(
    @field:NotBlank(message = "Username is required")
    @field:Email(message = "Username must be a valid email")
    @field:Schema(description = "Email do usuário (será o username)", example = "garcom@openbar.com")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    @field:Schema(description = "Senha do usuário (mínimo 6 caracteres)", example = "123456")
    val password: String,

    @field:NotNull(message = "Role is required")
    @field:Schema(description = "Função do usuário no sistema", example = "WAITER")
    val role: UserRole
)
