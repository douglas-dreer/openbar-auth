package com.openbar.auth.config

import com.openbar.auth.domain.model.UserRole
import com.openbar.auth.security.JwtTokenProvider
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `should return 401 when no token provided`() {
        mockMvc.get("/api/v1/auth/users") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return 401 when invalid token provided`() {
        mockMvc.get("/api/v1/auth/users") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer invalid-token")
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return 403 when user lacks required role`() {
        val token = createToken("00000000-0000-0000-0000-000000000001", "testuser", "WAITER")

        mockMvc.get("/api/v1/auth/users") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `should allow ADMIN access to list users`() {
        val token = createToken("00000000-0000-0000-0000-000000000001", "admin", "ADMIN")

        mockMvc.get("/api/v1/auth/users") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `should allow MANAGER access to list users`() {
        val token = createToken("00000000-0000-0000-0000-000000000002", "manager", "MANAGER")

        mockMvc.get("/api/v1/auth/users") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `should deny WAITER access to create user`() {
        val token = createToken("00000000-0000-0000-0000-000000000003", "waiter", "WAITER")

        mockMvc.get("/api/v1/auth/users") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isForbidden() }
        }
    }

    private fun createToken(userId: String, username: String, role: String): String {
        val user = com.openbar.auth.domain.model.User(
            id = UUID.fromString(userId),
            username = username,
            passwordHash = "\$2a\$12\$hashedPassword",
            role = UserRole.valueOf(role)
        )
        return jwtTokenProvider.generateToken(user)
    }
}
