package com.openbar.auth.repository

import com.openbar.auth.domain.model.User
import com.openbar.auth.domain.model.UserRole
import com.openbar.auth.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.Pageable
import java.util.UUID

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `should save and retrieve user`() {
        val user = User(
            username = "admin@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.ADMIN
        )

        val saved = userRepository.save(user)

        assertNotNull(saved.id)
        assertEquals("admin@example.com", saved.username)
        assertEquals(UserRole.ADMIN, saved.role)
        assertTrue(saved.active)
    }

    @Test
    fun `should find user by username`() {
        val user = User(
            username = "admin@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.ADMIN
        )

        userRepository.save(user)

        val found = userRepository.findByUsername("admin@example.com")

        assertTrue(found.isPresent)
        assertEquals("admin@example.com", found.get().username)
    }

    @Test
    fun `should return empty for non-existent username`() {
        val found = userRepository.findByUsername("nonexistent@example.com")

        assertFalse(found.isPresent)
    }

    @Test
    fun `should check if username exists`() {
        val user = User(
            username = "admin@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.ADMIN
        )

        userRepository.save(user)

        assertTrue(userRepository.existsByUsername("admin@example.com"))
        assertFalse(userRepository.existsByUsername("other@example.com"))
    }

    @Test
    fun `should find only active users`() {
        val activeUser = User(
            username = "active@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.WAITER,
            active = true
        )

        val inactiveUser = User(
            username = "inactive@example.com",
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.WAITER,
            active = false
        )

        userRepository.save(activeUser)
        userRepository.save(inactiveUser)

        val activeUsers = userRepository.findByActiveTrue(Pageable.unpaged())

        assertEquals(1, activeUsers.content.size)
        assertEquals("active@example.com", activeUsers.content[0].username)
    }

}
