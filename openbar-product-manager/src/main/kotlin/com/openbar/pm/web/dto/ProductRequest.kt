package com.openbar.pm.web.dto

import com.openbar.pm.domain.model.Routing
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "Dados para criação/ atualização de produto")
data class ProductRequest(
    @field:NotBlank(message = "Name is required")
    @field:Schema(description = "Nome do produto", example = "Caipirinha de Limão")
    val name: String,

    @field:Schema(description = "Descrição do produto")
    val description: String? = null,

    @field:NotNull(message = "Price is required")
    @field:Positive(message = "Price must be positive")
    @field:Schema(description = "Preço do produto", example = "25.00")
    val price: BigDecimal,

    @field:NotNull(message = "Category ID is required")
    @field:Schema(description = "ID da categoria")
    val categoryId: UUID,

    @field:NotNull(message = "Routing is required")
    @field:Schema(description = "Rota do produto (KITCHEN ou COUNTER)")
    val routing: Routing
)
