package com.openbar.auth.web.controller

import com.openbar.auth.service.AuthService
import com.openbar.auth.web.dto.LoginRequest
import com.openbar.auth.web.dto.LoginResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Autenticação e JWT")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    @Operation(
        summary = "Autenticar usuário",
        description = "Realiza login e retorna um token JWT válido por 1 hora",
        responses = [
            ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            ApiResponse(responseCode = "400", description = "Credenciais inválidas ou dados ausentes")
        ]
    )
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }
}
