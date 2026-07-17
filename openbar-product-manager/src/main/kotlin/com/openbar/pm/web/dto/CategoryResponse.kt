package com.openbar.pm.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Resposta de categoria")
data class CategoryResponse(
    @field:Schema(description = "ID da categoria")
    val id: UUID,

    @field:Schema(description = "Nome da categoria")
    val name: String,

    @field:Schema(description = "Descrição da categoria")
    val description: String?,

    @field:Schema(description = "Status ativo")
    val active: Boolean,

    @field:Schema(description = "Quantidade de produtos na categoria")
    val productCount: Long
)
