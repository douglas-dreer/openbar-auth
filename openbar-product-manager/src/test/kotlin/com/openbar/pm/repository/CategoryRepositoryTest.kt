package com.openbar.pm.repository

import com.openbar.pm.domain.model.Category
import com.openbar.pm.domain.repository.CategoryRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CategoryRepositoryTest @Autowired constructor(
    private val categoryRepository: CategoryRepository
) {

    @Test
    fun `should save and retrieve category`() {
        val category = Category(name = "Test Category", description = "Test Description")
        val saved = categoryRepository.save(category)

        assertNotNull(saved.id)
        assertEquals("Test Category", saved.name)
        assertTrue(saved.active)
    }

    @Test
    fun `should find only active categories`() {
        categoryRepository.save(Category(name = "Active", active = true))
        categoryRepository.save(Category(name = "Inactive", active = false))

        val activeCategories = categoryRepository.findByActiveTrue()
        assertTrue(activeCategories.all { it.active })
    }

    @Test
    fun `should find category by name case insensitive`() {
        categoryRepository.save(Category(name = "Unique Test Category"))

        val found = categoryRepository.findByNameIgnoreCase("unique test category")
        assertNotNull(found)
        assertEquals("Unique Test Category", found?.name)
    }

    @Test
    fun `should return null for non-existent name`() {
        val found = categoryRepository.findByNameIgnoreCase("nonexistent")
        assertNull(found)
    }
}
