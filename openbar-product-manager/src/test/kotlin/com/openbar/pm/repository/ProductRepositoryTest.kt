package com.openbar.pm.repository

import com.openbar.pm.domain.model.Category
import com.openbar.pm.domain.model.Product
import com.openbar.pm.domain.model.Routing
import com.openbar.pm.domain.repository.CategoryRepository
import com.openbar.pm.domain.repository.ProductRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProductRepositoryTest @Autowired constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {

    private fun createCategory(name: String = "Test Category"): Category {
        return categoryRepository.save(Category(name = name))
    }

    @Test
    fun `should save and retrieve product`() {
        val category = createCategory()
        val product = Product(
            name = "Test Product",
            price = BigDecimal("25.00"),
            category = category,
            routing = Routing.COUNTER
        )
        val saved = productRepository.save(product)

        assertNotNull(saved.id)
        assertEquals("Test Product", saved.name)
        assertEquals(BigDecimal("25.00"), saved.price)
        assertEquals(Routing.COUNTER, saved.routing)
        assertTrue(saved.active)
    }

    @Test
    fun `should find only active products`() {
        val category = createCategory()
        productRepository.save(Product(name = "Active", price = BigDecimal("10.00"), category = category, routing = Routing.KITCHEN, active = true))
        productRepository.save(Product(name = "Inactive", price = BigDecimal("10.00"), category = category, routing = Routing.KITCHEN, active = false))

        val activeProducts = productRepository.findByActiveTrue(PageRequest.of(0, 10))
        assertTrue(activeProducts.content.all { it.active })
    }

    @Test
    fun `should find products by category`() {
        val cat1 = createCategory("Cat1")
        val cat2 = createCategory("Cat2")
        productRepository.save(Product(name = "P1", price = BigDecimal("10.00"), category = cat1, routing = Routing.KITCHEN))
        productRepository.save(Product(name = "P2", price = BigDecimal("20.00"), category = cat2, routing = Routing.COUNTER))

        val result = productRepository.findByCategoryIdAndActiveTrue(cat1.id!!, PageRequest.of(0, 10))
        assertEquals(1, result.totalElements)
        assertEquals("P1", result.content[0].name)
    }

    @Test
    fun `should count products by category`() {
        val category = createCategory()
        productRepository.save(Product(name = "P1", price = BigDecimal("10.00"), category = category, routing = Routing.KITCHEN))
        productRepository.save(Product(name = "P2", price = BigDecimal("20.00"), category = category, routing = Routing.COUNTER))

        val count = productRepository.countByCategoryId(category.id!!)
        assertEquals(2, count)
    }
}
