package com.openbar.auth.web.controller

import com.openbar.auth.config.CustomPermissionEvaluator
import com.openbar.auth.config.RateLimitFilter
import com.openbar.auth.security.JwtAuthenticationEntryPoint
import com.openbar.auth.security.JwtAuthenticationFilter
import com.openbar.auth.security.JwtTokenProvider
import com.openbar.auth.service.AuthService
import com.openbar.auth.service.RefreshTokenService
import com.openbar.auth.web.dto.LoginRequest
import com.openbar.auth.web.dto.LoginResponse
import com.openbar.auth.web.dto.RefreshTokenRequest
import com.openbar.auth.web.dto.RefreshTokenResponse
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
import java.time.Instant
import java.util.UUID

@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var authService: AuthService

    @MockitoBean
    lateinit var refreshTokenService: RefreshTokenService

    @MockitoBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @MockitoBean
    lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @MockitoBean
    lateinit var jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint

    @MockitoBean
    lateinit var customPermissionEvaluator: CustomPermissionEvaluator

    @MockitoBean
    lateinit var rateLimitFilter: RateLimitFilter

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `login should return token for valid request`() {
        val refreshToken = UUID.randomUUID()
        val loginResponse = LoginResponse(
            accessToken = "mock-jwt-token",
            refreshToken = refreshToken,
            expiresIn = 3600L,
            refreshExpiresAt = Instant.now().plusMillis(604800000)
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
            jsonPath("$.refreshToken") { exists() }
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

    @Test
    fun `refresh should return new tokens`() {
        val refreshTokenValue = UUID.randomUUID()
        val newRefreshToken = UUID.randomUUID()
        val refreshResponse = RefreshTokenResponse(
            accessToken = "new-access-token",
            refreshToken = newRefreshToken,
            expiresIn = 3600L,
            refreshExpiresAt = Instant.now().plusMillis(604800000)
        )

        whenever(refreshTokenService.refresh(any()))
            .thenReturn(refreshResponse)

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                RefreshTokenRequest(refreshToken = refreshTokenValue)
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("new-access-token") }
            jsonPath("$.refreshToken") { exists() }
        }
    }

    @Test
    fun `refresh should return 400 for invalid token`() {
        whenever(refreshTokenService.refresh(any()))
            .thenThrow(IllegalArgumentException("Invalid or revoked refresh token"))

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                RefreshTokenRequest(refreshToken = UUID.randomUUID())
            )
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `logout should return 204`() {
        mockMvc.post("/api/v1/auth/logout") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                RefreshTokenRequest(refreshToken = UUID.randomUUID())
            )
        }.andExpect {
            status { isNoContent() }
        }
    }
}
