package com.openbar.pm.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Dados para criação/ atualização de categoria")
data class CategoryRequest(
    @field:NotBlank(message = "Name is required")
    @field:Schema(description = "Nome da categoria", example = "Bebidas")
    val name: String,

    @field:Schema(description = "Descrição da categoria")
    val description: String? = null
)
