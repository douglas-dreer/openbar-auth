package com.openbar.auth.web.controller

import com.openbar.auth.config.CustomPermissionEvaluator
import com.openbar.auth.security.JwtAuthenticationEntryPoint
import com.openbar.auth.security.JwtAuthenticationFilter
import com.openbar.auth.security.JwtTokenProvider
import com.openbar.auth.service.AuthService
import com.openbar.auth.web.dto.LoginRequest
import com.openbar.auth.web.dto.LoginResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var authService: AuthService

    @MockitoBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @MockitoBean
    lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @MockitoBean
    lateinit var jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint

    @MockitoBean
    lateinit var customPermissionEvaluator: CustomPermissionEvaluator

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `login should return token for valid request`() {
        val loginResponse = LoginResponse(
            accessToken = "mock-jwt-token",
            expiresIn = 3600L
        )

        whenever(authService.login(any()))
            .thenReturn(loginResponse)

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(username = "admin@example.com", password = "password123")
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("mock-jwt-token") }
            jsonPath("$.expiresIn") { value(3600) }
        }
    }

    @Test
    fun `login should return 400 for invalid request`() {
        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf("username" to "", "password" to "")
            )
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `login should return 400 for invalid credentials`() {
        whenever(authService.login(any()))
            .thenThrow(IllegalArgumentException("Invalid username or password"))

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(username = "wrong@example.com", password = "wrongpassword")
            )
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
