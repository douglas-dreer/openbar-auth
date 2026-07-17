package com.openbar.pm.web.controller

import com.openbar.pm.service.CategoryService
import com.openbar.pm.web.dto.CategoryRequest
import com.openbar.pm.web.dto.CategoryResponse
import com.openbar.pm.web.handler.GlobalExceptionHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.UUID

@WebMvcTest(CategoryController::class)
@Import(GlobalExceptionHandler::class)
class CategoryControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockitoBean
    private lateinit var categoryService: CategoryService

    @Test
    fun `should list categories`() {
        val response = listOf(
            CategoryResponse(UUID.randomUUID(), "Bebidas", "Bebidas em geral", true, 5)
        )
        whenever(categoryService.findAll()).thenReturn(response)

        mockMvc.get("/api/v1/pm/categories") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].name") { value("Bebidas") }
            jsonPath("$[0].productCount") { value(5) }
        }
    }

    @Test
    fun `should create category`() {
        val request = CategoryRequest(name = "Bebidas", description = "Bebidas em geral")
        val response = CategoryResponse(UUID.randomUUID(), "Bebidas", "Bebidas em geral", true, 0)
        whenever(categoryService.create(any())).thenReturn(response)

        mockMvc.post("/api/v1/pm/categories") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.name") { value("Bebidas") }
        }
    }

    @Test
    fun `should return 400 when name is blank`() {
        val request = CategoryRequest(name = "")

        mockMvc.post("/api/v1/pm/categories") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should update category`() {
        val id = UUID.randomUUID()
        val request = CategoryRequest(name = "Updated", description = "Updated desc")
        val response = CategoryResponse(id, "Updated", "Updated desc", true, 0)
        whenever(categoryService.update(eq(id), any())).thenReturn(response)

        mockMvc.put("/api/v1/pm/categories/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Updated") }
        }
    }
}
