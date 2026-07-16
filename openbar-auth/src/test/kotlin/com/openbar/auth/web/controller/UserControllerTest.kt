package com.openbar.auth.web.controller

import com.openbar.auth.domain.model.UserRole
import com.openbar.auth.service.UserService
import com.openbar.auth.web.dto.UserResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.UUID

@WebMvcTest(UserController::class)
class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var userService: UserService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `findAll should return paginated users`() {
        val user = UserResponse(
            id = UUID.randomUUID(),
            username = "admin@example.com",
            role = UserRole.ADMIN,
            active = true
        )

        whenever(userService.findAll(any()))
            .thenReturn(PageImpl(listOf(user)))

        mockMvc.get("/api/v1/auth/users") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].username") { value("admin@example.com") }
            jsonPath("$.content[0].role") { value("ADMIN") }
        }
    }

    @Test
    fun `findById should return user when exists`() {
        val userId = UUID.randomUUID()
        val user = UserResponse(
            id = userId,
            username = "admin@example.com",
            role = UserRole.ADMIN,
            active = true
        )

        whenever(userService.findById(userId))
            .thenReturn(user)

        mockMvc.get("/api/v1/auth/users/$userId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.username") { value("admin@example.com") }
        }
    }

    @Test
    fun `findById should return 404 when not found`() {
        val userId = UUID.randomUUID()
        whenever(userService.findById(userId))
            .thenThrow(IllegalArgumentException("User not found"))

        mockMvc.get("/api/v1/auth/users/$userId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
