package com.openbar.auth.security

import com.openbar.auth.domain.model.User
import com.openbar.auth.domain.model.UserRole
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val secret = "myDefaultSecretKeyForDevelopmentOnlyDoNotUseInProduction2024!"
    private val expirationMs = 3600000L

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = JwtTokenProvider(secret, expirationMs)
    }

    private fun createUser(
        id: UUID = UUID.randomUUID(),
        username: String = "admin@example.com",
        role: UserRole = UserRole.ADMIN
    ) = User(id = id, username = username, passwordHash = "hash", role = role)

    @Test
    fun `generateToken should return non-empty token`() {
        val user = createUser()
        val token = jwtTokenProvider.generateToken(user)
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
    }

    @Test
    fun `getUserIdFromToken should return user id`() {
        val userId = UUID.randomUUID()
        val user = createUser(id = userId)
        val token = jwtTokenProvider.generateToken(user)

        val result = jwtTokenProvider.getUserIdFromToken(token)

        assertEquals(userId.toString(), result)
    }

    @Test
    fun `getUsernameFromToken should return username`() {
        val user = createUser(username = "test@example.com")
        val token = jwtTokenProvider.generateToken(user)

        val result = jwtTokenProvider.getUsernameFromToken(token)

        assertEquals("test@example.com", result)
    }

    @Test
    fun `getRoleFromToken should return role`() {
        val user = createUser(role = UserRole.WAITER)
        val token = jwtTokenProvider.generateToken(user)

        val result = jwtTokenProvider.getRoleFromToken(token)

        assertEquals("WAITER", result)
    }

    @Test
    fun `validateToken should return true for valid token`() {
        val token = jwtTokenProvider.generateToken(createUser())
        assertTrue(jwtTokenProvider.validateToken(token))
    }

    @Test
    fun `validateToken should return false for invalid token`() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"))
    }

    @Test
    fun `validateToken should return false for malformed token`() {
        assertFalse(jwtTokenProvider.validateToken("not.a.jwt"))
    }

    @Test
    fun `validateToken should return false for token with wrong signature`() {
        val otherProvider = JwtTokenProvider("anotherSecretKeyThatIsDifferentAndLongEnoughForHmac2024!", expirationMs)
        val token = otherProvider.generateToken(createUser())

        assertFalse(jwtTokenProvider.validateToken(token))
    }

    @Test
    fun `getExpirationMs should return configured expiration`() {
        assertEquals(expirationMs, jwtTokenProvider.getExpirationMs())
    }

    @Test
    fun `generated token should contain all claims`() {
        val userId = UUID.randomUUID()
        val user = createUser(id = userId, username = "claim@test.com", role = UserRole.CASHIER)
        val token = jwtTokenProvider.generateToken(user)

        assertEquals(userId.toString(), jwtTokenProvider.getUserIdFromToken(token))
        assertEquals("claim@test.com", jwtTokenProvider.getUsernameFromToken(token))
        assertEquals("CASHIER", jwtTokenProvider.getRoleFromToken(token))
    }

    @Test
    fun `getJtiFromToken should return unique jti`() {
        val user = createUser()
        val token1 = jwtTokenProvider.generateToken(user)
        val token2 = jwtTokenProvider.generateToken(user)

        val jti1 = jwtTokenProvider.getJtiFromToken(token1)
        val jti2 = jwtTokenProvider.getJtiFromToken(token2)

        assertNotNull(jti1)
        assertNotNull(jti2)
        assertNotEquals(jti1, jti2)
    }

    @Test
    fun `getExpirationFromToken should return expiration date`() {
        val user = createUser()
        val token = jwtTokenProvider.generateToken(user)

        val expiration = jwtTokenProvider.getExpirationFromToken(token)

        assertNotNull(expiration)
        assertTrue(expiration.after(java.util.Date()))
    }
}
