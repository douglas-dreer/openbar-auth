package com.openbar.auth.web.dto

import com.openbar.auth.domain.model.UserRole
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Dados do usuário retornado")
data class UserResponse(
    @field:Schema(description = "ID único do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Email/username do usuário", example = "garcom@openbar.com")
    val username: String,

    @field:Schema(description = "Função do usuário", example = "WAITER")
    val role: UserRole,

    @field:Schema(description = "Se o usuário está ativo", example = "true")
    val active: Boolean
)
