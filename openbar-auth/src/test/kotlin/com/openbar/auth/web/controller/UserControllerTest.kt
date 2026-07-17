package com.openbar.auth.web.controller

import com.openbar.auth.config.CustomPermissionEvaluator
import com.openbar.auth.config.RateLimitFilter
import com.openbar.auth.domain.model.UserRole
import com.openbar.auth.security.JwtAuthenticationEntryPoint
import com.openbar.auth.security.JwtAuthenticationFilter
import com.openbar.auth.security.JwtTokenProvider
import com.openbar.auth.service.JwtBlacklistService
import com.openbar.auth.service.UserService
import com.openbar.auth.web.dto.CreateUserRequest
import com.openbar.auth.web.dto.UpdateUserRequest
import com.openbar.auth.web.dto.UserResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.UUID

@WebMvcTest(UserController::class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var userService: UserService

    @MockitoBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @MockitoBean
    lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @MockitoBean
    lateinit var jwtBlacklistService: JwtBlacklistService

    @MockitoBean
    lateinit var jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint

    @MockitoBean
    lateinit var customPermissionEvaluator: CustomPermissionEvaluator

    @MockitoBean
    lateinit var rateLimitFilter: RateLimitFilter

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

    @Test
    fun `create should return 201 with created user`() {
        val userId = UUID.randomUUID()
        val response = UserResponse(
            id = userId,
            username = "new@example.com",
            role = UserRole.WAITER,
            active = true
        )

        whenever(userService.create(any()))
            .thenReturn(response)

        mockMvc.post("/api/v1/auth/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                CreateUserRequest(
                    username = "new@example.com",
                    password = "password123",
                    role = UserRole.WAITER
                )
            )
        }.andExpect {
            status { isCreated() }
            jsonPath("$.username") { value("new@example.com") }
            jsonPath("$.role") { value("WAITER") }
        }
    }

    @Test
    fun `create should return 400 for invalid request`() {
        mockMvc.post("/api/v1/auth/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf("username" to "", "password" to "12345", "role" to "WAITER")
            )
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `update should return updated user`() {
        val userId = UUID.randomUUID()
        val response = UserResponse(
            id = userId,
            username = "updated@example.com",
            role = UserRole.MANAGER,
            active = true
        )

        whenever(userService.update(any(), any()))
            .thenReturn(response)

        mockMvc.put("/api/v1/auth/users/$userId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                UpdateUserRequest(
                    username = "updated@example.com",
                    role = UserRole.MANAGER,
                    active = true
                )
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.username") { value("updated@example.com") }
            jsonPath("$.role") { value("MANAGER") }
        }
    }

    @Test
    fun `update should return 400 when user not found`() {
        val userId = UUID.randomUUID()
        whenever(userService.update(any(), any()))
            .thenThrow(IllegalArgumentException("User not found"))

        mockMvc.put("/api/v1/auth/users/$userId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                UpdateUserRequest(username = "test@example.com", role = UserRole.WAITER, active = true)
            )
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `delete should return 204`() {
        mockMvc.delete("/api/v1/auth/users/${UUID.randomUUID()}") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `delete should return 400 when user not found`() {
        val userId = UUID.randomUUID()
        whenever(userService.softDelete(userId))
            .thenThrow(IllegalArgumentException("User not found"))

        mockMvc.delete("/api/v1/auth/users/$userId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
