package com.openbar.pm.domain.repository

import com.openbar.pm.domain.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProductRepository : JpaRepository<Product, UUID> {
    fun findByActiveTrue(pageable: Pageable): Page<Product>
    fun findByCategoryIdAndActiveTrue(categoryId: UUID, pageable: Pageable): Page<Product>
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Product>
    fun countByCategoryId(categoryId: UUID): Long
}
