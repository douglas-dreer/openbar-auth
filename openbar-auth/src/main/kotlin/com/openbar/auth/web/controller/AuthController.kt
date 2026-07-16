package com.openbar.auth.web.controller

import com.openbar.auth.service.AuthService
import com.openbar.auth.service.RefreshTokenService
import com.openbar.auth.web.dto.LoginRequest
import com.openbar.auth.web.dto.LoginResponse
import com.openbar.auth.web.dto.RefreshTokenRequest
import com.openbar.auth.web.dto.RefreshTokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Autenticação e JWT")
class AuthController(
    private val authService: AuthService,
    private val refreshTokenService: RefreshTokenService
) {

    @PostMapping("/login")
    @Operation(
        summary = "Autenticar usuário",
        description = "Realiza login e retorna um token JWT válido por 1 hora e refresh token válido por 7 dias",
        responses = [
            ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            ApiResponse(responseCode = "400", description = "Credenciais inválidas ou dados ausentes")
        ]
    )
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Renovar access token",
        description = "Utiliza o refresh token para obter um novo access token e refresh token",
        responses = [
            ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
            ApiResponse(responseCode = "400", description = "Refresh token inválido ou expirado")
        ]
    )
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<RefreshTokenResponse> {
        val response = refreshTokenService.refresh(request.refreshToken)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Encerrar sessão",
        description = "Revoga o refresh token, encerrando a sessão do usuário",
        responses = [
            ApiResponse(responseCode = "204", description = "Sessão encerrada com sucesso"),
            ApiResponse(responseCode = "400", description = "Refresh token inválido")
        ]
    )
    fun logout(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<Void> {
        authService.logout(request.refreshToken)
        return ResponseEntity.noContent().build()
    }
}
