package com.openbar.pm.web.controller

import com.openbar.pm.service.CategoryService
import com.openbar.pm.web.dto.CategoryRequest
import com.openbar.pm.web.dto.CategoryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/pm/categories")
@Tag(name = "Categories", description = "Gestão de categorias de produtos")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    @Operation(summary = "Listar categorias ativas")
    fun findAll(): ResponseEntity<List<CategoryResponse>> {
        return ResponseEntity.ok(categoryService.findAll())
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    fun findById(@PathVariable id: UUID): ResponseEntity<CategoryResponse> {
        return ResponseEntity.ok(categoryService.findById(id))
    }

    @PostMapping
    @Operation(summary = "Criar categoria")
    fun create(@Valid @RequestBody request: CategoryRequest): ResponseEntity<CategoryResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: CategoryRequest): ResponseEntity<CategoryResponse> {
        return ResponseEntity.ok(categoryService.update(id, request))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar categoria")
    fun deactivate(@PathVariable id: UUID): ResponseEntity<Void> {
        categoryService.deactivate(id)
        return ResponseEntity.noContent().build()
    }
}
