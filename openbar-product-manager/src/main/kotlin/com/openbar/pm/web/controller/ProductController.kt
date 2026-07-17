package com.openbar.pm.web.controller

import com.openbar.pm.service.ProductService
import com.openbar.pm.web.dto.ProductRequest
import com.openbar.pm.web.dto.ProductResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/pm/products")
@Tag(name = "Products", description = "Gestão de produtos")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    @Operation(summary = "Listar produtos ativos")
    fun findAll(
        @RequestParam(required = false) categoryId: UUID?,
        @RequestParam(required = false) search: String?,
        pageable: Pageable
    ): ResponseEntity<Page<ProductResponse>> {
        val result = when {
            categoryId != null -> productService.findByCategory(categoryId, pageable)
            search != null -> productService.search(search, pageable)
            else -> productService.findAll(pageable)
        }
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    fun findById(@PathVariable id: UUID): ResponseEntity<ProductResponse> {
        return ResponseEntity.ok(productService.findById(id))
    }

    @PostMapping
    @Operation(summary = "Criar produto")
    fun create(@Valid @RequestBody request: ProductRequest): ResponseEntity<ProductResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: ProductRequest): ResponseEntity<ProductResponse> {
        return ResponseEntity.ok(productService.update(id, request))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar produto")
    fun deactivate(@PathVariable id: UUID): ResponseEntity<Void> {
        productService.deactivate(id)
        return ResponseEntity.noContent().build()
    }
}
