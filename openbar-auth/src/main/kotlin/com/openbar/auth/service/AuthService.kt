package com.openbar.auth.service

import com.openbar.auth.domain.model.User
import com.openbar.auth.domain.repository.UserRepository
import com.openbar.auth.security.JwtTokenProvider
import com.openbar.auth.web.dto.LoginRequest
import com.openbar.auth.web.dto.LoginResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService
) {

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByUsername(request.username)
            .orElseThrow { IllegalArgumentException("Invalid username or password") }

        require(user.active) { "Account is deactivated" }

        require(passwordEncoder.matches(request.password, user.passwordHash)) { "Invalid username or password" }

        val token = jwtTokenProvider.generateToken(user)
        val refreshToken = refreshTokenService.createRefreshToken(user)

        return LoginResponse(
            accessToken = token,
            refreshToken = refreshToken.token!!,
            expiresIn = jwtTokenProvider.getExpirationMs() / 1000,
            refreshExpiresAt = refreshToken.expiresAt
        )
    }

    fun logout(refreshToken: java.util.UUID) {
        refreshTokenService.revokeRefreshToken(refreshToken)
    }
}
