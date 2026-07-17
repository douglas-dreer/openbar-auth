package com.openbar.pm.web.dto

import com.openbar.pm.domain.model.Routing
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "Resposta de produto")
data class ProductResponse(
    @field:Schema(description = "ID do produto")
    val id: UUID,

    @field:Schema(description = "Nome do produto")
    val name: String,

    @field:Schema(description = "Descrição do produto")
    val description: String?,

    @field:Schema(description = "Preço do produto")
    val price: BigDecimal,

    @field:Schema(description = "ID da categoria")
    val categoryId: UUID,

    @field:Schema(description = "Nome da categoria")
    val categoryName: String,

    @field:Schema(description = "Rota do produto")
    val routing: Routing,

    @field:Schema(description = "Status ativo")
    val active: Boolean
)
