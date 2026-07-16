package com.openbar.auth.service

import com.openbar.auth.domain.model.User
import com.openbar.auth.domain.model.UserRole
import com.openbar.auth.domain.repository.UserRepository
import com.openbar.auth.security.JwtTokenProvider
import com.openbar.auth.web.dto.LoginRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional
import java.util.UUID

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        passwordEncoder = mock()
        jwtTokenProvider = mock()
        authService = AuthService(userRepository, passwordEncoder, jwtTokenProvider)
    }

    @Test
    fun `login should return token for valid credentials`() {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            username = "admin@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.ADMIN,
            active = true
        )

        whenever(userRepository.findByUsername("admin@example.com"))
            .thenReturn(Optional.of(user))
        whenever(passwordEncoder.matches("password123", user.passwordHash))
            .thenReturn(true)
        whenever(jwtTokenProvider.generateToken(user))
            .thenReturn("mock-jwt-token")
        whenever(jwtTokenProvider.getExpirationMs())
            .thenReturn(3600000L)

        val request = LoginRequest(username = "admin@example.com", password = "password123")
        val response = authService.login(request)

        assertEquals("mock-jwt-token", response.accessToken)
        assertEquals(3600L, response.expiresIn)
    }

    @Test
    fun `login should throw exception for invalid username`() {
        whenever(userRepository.findByUsername("invalid@example.com"))
            .thenReturn(Optional.empty())

        val request = LoginRequest(username = "invalid@example.com", password = "password123")

        assertThrows<IllegalArgumentException> {
            authService.login(request)
        }
    }

    @Test
    fun `login should throw exception for invalid password`() {
        val user = User(
            id = UUID.randomUUID(),
            username = "admin@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.ADMIN,
            active = true
        )

        whenever(userRepository.findByUsername("admin@example.com"))
            .thenReturn(Optional.of(user))
        whenever(passwordEncoder.matches("wrongpassword", user.passwordHash))
            .thenReturn(false)

        val request = LoginRequest(username = "admin@example.com", password = "wrongpassword")

        assertThrows<IllegalArgumentException> {
            authService.login(request)
        }
    }

    @Test
    fun `login should throw exception for inactive user`() {
        val user = User(
            id = UUID.randomUUID(),
            username = "inactive@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.WAITER,
            active = false
        )

        whenever(userRepository.findByUsername("inactive@example.com"))
            .thenReturn(Optional.of(user))

        val request = LoginRequest(username = "inactive@example.com", password = "password123")

        assertThrows<IllegalArgumentException> {
            authService.login(request)
        }
    }
}
