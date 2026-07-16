package com.openbar.auth.service

import com.openbar.auth.domain.model.User
import com.openbar.auth.domain.model.UserRole
import com.openbar.auth.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional
import java.util.UUID

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        passwordEncoder = mock()
        userService = UserService(userRepository, passwordEncoder)
    }

    @Test
    fun `findAll should return paginated users`() {
        val user = User(
            id = UUID.randomUUID(),
            username = "admin@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.ADMIN,
            active = true
        )

        whenever(userRepository.findByActiveTrue(any()))
            .thenReturn(PageImpl(listOf(user)))

        val result = userService.findAll(Pageable.unpaged())

        assertEquals(1, result.content.size)
        assertEquals("admin@example.com", result.content[0].username)
    }

    @Test
    fun `findById should return user when exists`() {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            username = "admin@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.ADMIN,
            active = true
        )

        whenever(userRepository.findById(userId))
            .thenReturn(Optional.of(user))

        val result = userService.findById(userId)

        assertEquals(userId, result.id)
        assertEquals("admin@example.com", result.username)
    }

    @Test
    fun `findById should throw exception when not found`() {
        val userId = UUID.randomUUID()
        whenever(userRepository.findById(userId))
            .thenReturn(Optional.empty())

        assertThrows<IllegalArgumentException> {
            userService.findById(userId)
        }
    }

    @Test
    fun `create should save new user`() {
        val userId = UUID.randomUUID()
        val savedUser = User(
            id = userId,
            username = "new@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.WAITER,
            active = true
        )

        whenever(userRepository.existsByUsername("new@example.com"))
            .thenReturn(false)
        whenever(passwordEncoder.encode("password123"))
            .thenReturn("\$2a\$12\$hashedPassword")
        whenever(userRepository.save(any()))
            .thenReturn(savedUser)

        val request = com.openbar.auth.web.dto.CreateUserRequest(
            username = "new@example.com",
            password = "password123",
            role = UserRole.WAITER
        )

        val result = userService.create(request)

        assertEquals("new@example.com", result.username)
        assertEquals(UserRole.WAITER, result.role)
        assertTrue(result.active)
    }

    @Test
    fun `create should throw exception for duplicate username`() {
        whenever(userRepository.existsByUsername("existing@example.com"))
            .thenReturn(true)

        val request = com.openbar.auth.web.dto.CreateUserRequest(
            username = "existing@example.com",
            password = "password123",
            role = UserRole.WAITER
        )

        assertThrows<IllegalArgumentException> {
            userService.create(request)
        }
    }

    @Test
    fun `softDelete should set active to false`() {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            username = "admin@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.ADMIN,
            active = true
        )

        whenever(userRepository.findById(userId))
            .thenReturn(Optional.of(user))
        whenever(userRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        userService.softDelete(userId)

        org.mockito.kotlin.verify(userRepository).save(any())
    }

    @Test
    fun `softDelete should throw when user not found`() {
        val userId = UUID.randomUUID()
        whenever(userRepository.findById(userId))
            .thenReturn(Optional.empty())

        assertThrows<IllegalArgumentException> {
            userService.softDelete(userId)
        }
    }

    @Test
    fun `update should modify user fields`() {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            username = "old@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.WAITER,
            active = true
        )

        whenever(userRepository.findById(userId))
            .thenReturn(Optional.of(user))
        whenever(userRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        val request = com.openbar.auth.web.dto.UpdateUserRequest(
            username = "new@example.com",
            role = UserRole.MANAGER,
            active = false
        )

        val result = userService.update(userId, request)

        assertEquals("new@example.com", result.username)
        assertEquals(UserRole.MANAGER, result.role)
        assertFalse(result.active)
    }

    @Test
    fun `update should keep original values when request fields are null`() {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            username = "keep@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.CASHIER,
            active = true
        )

        whenever(userRepository.findById(userId))
            .thenReturn(Optional.of(user))
        whenever(userRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        val request = com.openbar.auth.web.dto.UpdateUserRequest(
            username = null,
            role = null,
            active = null
        )

        val result = userService.update(userId, request)

        assertEquals("keep@example.com", result.username)
        assertEquals(UserRole.CASHIER, result.role)
        assertTrue(result.active)
    }

    @Test
    fun `update should throw when user not found`() {
        val userId = UUID.randomUUID()
        whenever(userRepository.findById(userId))
            .thenReturn(Optional.empty())

        val request = com.openbar.auth.web.dto.UpdateUserRequest(
            username = "new@example.com",
            role = UserRole.WAITER,
            active = true
        )

        assertThrows<IllegalArgumentException> {
            userService.update(userId, request)
        }
    }
}
