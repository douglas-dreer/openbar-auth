package com.openbar.pm.domain.repository

import com.openbar.pm.domain.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CategoryRepository : JpaRepository<Category, UUID> {
    fun findByActiveTrue(): List<Category>
    fun findByNameIgnoreCase(name: String): Category?
}
