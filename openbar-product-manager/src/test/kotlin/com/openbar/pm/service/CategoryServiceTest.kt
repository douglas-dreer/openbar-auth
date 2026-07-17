package com.openbar.pm.service

import com.openbar.pm.domain.model.Category
import com.openbar.pm.domain.repository.CategoryRepository
import com.openbar.pm.domain.repository.ProductRepository
import com.openbar.pm.web.dto.CategoryRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.util.Optional
import java.util.UUID

class CategoryServiceTest {

    private val categoryRepository = mock<CategoryRepository>()
    private val productRepository = mock<ProductRepository>()
    private val categoryService = CategoryService(categoryRepository, productRepository)

    @Test
    fun `should create category`() {
        val request = CategoryRequest(name = "Bebidas", description = "Bebidas em geral")
        val category = Category(id = UUID.randomUUID(), name = "Bebidas", description = "Bebidas em geral")

        whenever(categoryRepository.findByNameIgnoreCase("Bebidas")).thenReturn(null)
        whenever(categoryRepository.save(any())).thenReturn(category)
        whenever(productRepository.countByCategoryId(any())).thenReturn(0L)

        val response = categoryService.create(request)

        assertEquals("Bebidas", response.name)
        assertEquals("Bebidas em geral", response.description)
        verify(categoryRepository).save(any())
    }

    @Test
    fun `should throw when creating duplicate category`() {
        val request = CategoryRequest(name = "Bebidas")
        val existing = Category(id = UUID.randomUUID(), name = "Bebidas")

        whenever(categoryRepository.findByNameIgnoreCase("Bebidas")).thenReturn(existing)

        assertThrows<IllegalArgumentException> {
            categoryService.create(request)
        }
    }

    @Test
    fun `should update category`() {
        val id = UUID.randomUUID()
        val request = CategoryRequest(name = "Updated Name", description = "Updated")
        val category = Category(id = id, name = "Old Name")

        whenever(categoryRepository.findById(id)).thenReturn(Optional.of(category))
        whenever(categoryRepository.findByNameIgnoreCase("Updated Name")).thenReturn(null)
        whenever(categoryRepository.save(any())).thenReturn(category.copy(name = "Updated Name"))
        whenever(productRepository.countByCategoryId(id)).thenReturn(0L)

        val response = categoryService.update(id, request)

        assertEquals("Updated Name", response.name)
    }

    @Test
    fun `should deactivate category`() {
        val id = UUID.randomUUID()
        val category = Category(id = id, name = "Test")

        whenever(categoryRepository.findById(id)).thenReturn(Optional.of(category))
        whenever(productRepository.countByCategoryId(id)).thenReturn(0L)

        categoryService.deactivate(id)

        verify(categoryRepository).save(argThat { category -> !category.active })
    }

    @Test
    fun `should throw when deactivating category with products`() {
        val id = UUID.randomUUID()
        val category = Category(id = id, name = "Test")

        whenever(categoryRepository.findById(id)).thenReturn(Optional.of(category))
        whenever(productRepository.countByCategoryId(id)).thenReturn(5L)

        assertThrows<IllegalArgumentException> {
            categoryService.deactivate(id)
        }
    }
}
