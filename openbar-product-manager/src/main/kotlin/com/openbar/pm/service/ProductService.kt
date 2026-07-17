package com.openbar.pm.service

import com.openbar.pm.domain.model.Product
import com.openbar.pm.domain.repository.CategoryRepository
import com.openbar.pm.domain.repository.ProductRepository
import com.openbar.pm.web.dto.ProductRequest
import com.openbar.pm.web.dto.ProductResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {

    fun findAll(pageable: Pageable): Page<ProductResponse> {
        return productRepository.findByActiveTrue(pageable).map { toResponse(it) }
    }

    fun findByCategory(categoryId: UUID, pageable: Pageable): Page<ProductResponse> {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable).map { toResponse(it) }
    }

    fun search(name: String, pageable: Pageable): Page<ProductResponse> {
        return productRepository.findByNameContainingIgnoreCase(name, pageable).map { toResponse(it) }
    }

    fun findById(id: UUID): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product not found: $id") }
        return toResponse(product)
    }

    @Transactional
    fun create(request: ProductRequest): ProductResponse {
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { IllegalArgumentException("Category not found: ${request.categoryId}") }

        val product = Product(
            name = request.name,
            description = request.description,
            price = request.price,
            category = category,
            routing = request.routing
        )
        return toResponse(productRepository.save(product))
    }

    @Transactional
    fun update(id: UUID, request: ProductRequest): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product not found: $id") }

        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { IllegalArgumentException("Category not found: ${request.categoryId}") }

        val updated = product.copy(
            name = request.name,
            description = request.description,
            price = request.price,
            category = category,
            routing = request.routing
        )
        return toResponse(productRepository.save(updated))
    }

    @Transactional
    fun deactivate(id: UUID) {
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product not found: $id") }
        productRepository.save(product.copy(active = false))
    }

    private fun toResponse(product: Product): ProductResponse {
        return ProductResponse(
            id = product.id!!,
            name = product.name,
            description = product.description,
            price = product.price,
            categoryId = product.category.id!!,
            categoryName = product.category.name,
            routing = product.routing,
            active = product.active
        )
    }
}
