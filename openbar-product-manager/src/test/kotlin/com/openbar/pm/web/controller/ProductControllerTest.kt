package com.openbar.pm.web.controller

import com.openbar.pm.domain.model.Routing
import com.openbar.pm.service.ProductService
import com.openbar.pm.web.dto.ProductRequest
import com.openbar.pm.web.dto.ProductResponse
import com.openbar.pm.web.handler.GlobalExceptionHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.math.BigDecimal
import java.util.UUID

@WebMvcTest(ProductController::class)
@Import(GlobalExceptionHandler::class)
class ProductControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockitoBean
    private lateinit var productService: ProductService

    @Test
    fun `should list products`() {
        val categoryId = UUID.randomUUID()
        val response = listOf(
            ProductResponse(UUID.randomUUID(), "Caipirinha", null, BigDecimal("25.00"), categoryId, "Bebidas", Routing.COUNTER, true)
        )
        whenever(productService.findAll(any())).thenReturn(PageImpl(response))

        mockMvc.get("/api/v1/pm/products") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].name") { value("Caipirinha") }
            jsonPath("$.content[0].price") { value(25.00) }
        }
    }

    @Test
    fun `should create product`() {
        val categoryId = UUID.randomUUID()
        val request = ProductRequest(
            name = "Caipirinha",
            price = BigDecimal("25.00"),
            categoryId = categoryId,
            routing = Routing.COUNTER
        )
        val response = ProductResponse(
            UUID.randomUUID(), "Caipirinha", null, BigDecimal("25.00"), categoryId, "Bebidas", Routing.COUNTER, true
        )
        whenever(productService.create(any())).thenReturn(response)

        mockMvc.post("/api/v1/pm/products") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.name") { value("Caipirinha") }
            jsonPath("$.routing") { value("COUNTER") }
        }
    }

    @Test
    fun `should return 400 when product name is blank`() {
        val request = ProductRequest(
            name = "",
            price = BigDecimal("25.00"),
            categoryId = UUID.randomUUID(),
            routing = Routing.COUNTER
        )

        mockMvc.post("/api/v1/pm/products") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 400 when price is negative`() {
        val request = ProductRequest(
            name = "Test",
            price = BigDecimal("-10.00"),
            categoryId = UUID.randomUUID(),
            routing = Routing.COUNTER
        )

        mockMvc.post("/api/v1/pm/products") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
