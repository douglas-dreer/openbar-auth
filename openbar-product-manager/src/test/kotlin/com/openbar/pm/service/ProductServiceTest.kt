package com.openbar.pm.service

import com.openbar.pm.domain.model.Category
import com.openbar.pm.domain.model.Product
import com.openbar.pm.domain.model.Routing
import com.openbar.pm.domain.repository.CategoryRepository
import com.openbar.pm.domain.repository.ProductRepository
import com.openbar.pm.web.dto.ProductRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

class ProductServiceTest {

    private val productRepository = mock<ProductRepository>()
    private val categoryRepository = mock<CategoryRepository>()
    private val productService = ProductService(productRepository, categoryRepository)

    private fun createCategory(id: UUID = UUID.randomUUID()): Category {
        return Category(id = id, name = "Bebidas")
    }

    @Test
    fun `should create product`() {
        val categoryId = UUID.randomUUID()
        val category = createCategory(categoryId)
        val request = ProductRequest(
            name = "Caipirinha",
            price = BigDecimal("25.00"),
            categoryId = categoryId,
            routing = Routing.COUNTER
        )
        val product = Product(
            id = UUID.randomUUID(),
            name = "Caipirinha",
            price = BigDecimal("25.00"),
            category = category,
            routing = Routing.COUNTER
        )

        whenever(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category))
        whenever(productRepository.save(any())).thenReturn(product)

        val response = productService.create(request)

        assertEquals("Caipirinha", response.name)
        assertEquals(BigDecimal("25.00"), response.price)
        assertEquals(categoryId, response.categoryId)
    }

    @Test
    fun `should throw when creating product with non-existent category`() {
        val request = ProductRequest(
            name = "Caipirinha",
            price = BigDecimal("25.00"),
            categoryId = UUID.randomUUID(),
            routing = Routing.COUNTER
        )

        whenever(categoryRepository.findById(any())).thenReturn(Optional.empty())

        assertThrows<IllegalArgumentException> {
            productService.create(request)
        }
    }

    @Test
    fun `should find all products`() {
        val category = createCategory()
        val product = Product(
            id = UUID.randomUUID(),
            name = "Test",
            price = BigDecimal("10.00"),
            category = category,
            routing = Routing.KITCHEN
        )

        whenever(productRepository.findByActiveTrue(any())).thenReturn(PageImpl(listOf(product)))

        val result = productService.findAll(PageRequest.of(0, 10))

        assertEquals(1, result.totalElements)
        assertEquals("Test", result.content[0].name)
    }
}
