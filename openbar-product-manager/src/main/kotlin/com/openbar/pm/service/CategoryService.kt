package com.openbar.pm.service

import com.openbar.pm.domain.model.Category
import com.openbar.pm.domain.repository.CategoryRepository
import com.openbar.pm.domain.repository.ProductRepository
import com.openbar.pm.web.dto.CategoryRequest
import com.openbar.pm.web.dto.CategoryResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository
) {

    fun findAll(): List<CategoryResponse> {
        return categoryRepository.findByActiveTrue().map { toResponse(it) }
    }

    fun findById(id: UUID): CategoryResponse {
        val category = categoryRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Category not found: $id") }
        return toResponse(category)
    }

    @Transactional
    fun create(request: CategoryRequest): CategoryResponse {
        val existing = categoryRepository.findByNameIgnoreCase(request.name)
        require(existing == null) { "Category name already exists: ${request.name}" }

        val category = Category(
            name = request.name,
            description = request.description
        )
        return toResponse(categoryRepository.save(category))
    }

    @Transactional
    fun update(id: UUID, request: CategoryRequest): CategoryResponse {
        val category = categoryRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Category not found: $id") }

        val existing = categoryRepository.findByNameIgnoreCase(request.name)
        require(existing == null || existing.id == id) { "Category name already exists: ${request.name}" }

        val updated = category.copy(
            name = request.name,
            description = request.description
        )
        return toResponse(categoryRepository.save(updated))
    }

    @Transactional
    fun deactivate(id: UUID) {
        val category = categoryRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Category not found: $id") }

        val productCount = productRepository.countByCategoryId(id)
        require(productCount == 0L) { "Cannot deactivate category with $productCount products" }

        categoryRepository.save(category.copy(active = false))
    }

    private fun toResponse(category: Category): CategoryResponse {
        val productCount = productRepository.countByCategoryId(category.id!!)
        return CategoryResponse(
            id = category.id,
            name = category.name,
            description = category.description,
            active = category.active,
            productCount = productCount
        )
    }
}
