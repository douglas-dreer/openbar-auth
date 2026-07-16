package com.openbar.auth.web.controller

import com.openbar.auth.service.UserService
import com.openbar.auth.web.dto.CreateUserRequest
import com.openbar.auth.web.dto.UpdateUserRequest
import com.openbar.auth.web.dto.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth/users")
@Tag(name = "Users", description = "CRUD de usuários/funcionários")
class UserController(
    private val userService: UserService
) {

    @GetMapping
    @Operation(
        summary = "Listar usuários",
        description = "Retorna lista paginada de usuários ativos",
        responses = [
            ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
        ]
    )
    fun findAll(pageable: Pageable): ResponseEntity<Page<UserResponse>> {
        return ResponseEntity.ok(userService.findAll(pageable))
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar usuário por ID",
        description = "Retorna os dados de um usuário específico",
        responses = [
            ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            ApiResponse(responseCode = "400", description = "Usuário não encontrado")
        ]
    )
    fun findById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.findById(id))
    }

    @PostMapping
    @Operation(
        summary = "Criar usuário",
        description = "Cadastra um novo funcionário no sistema",
        responses = [
            ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            ApiResponse(responseCode = "400", description = "Dados inválidos ou username já existe")
        ]
    )
    fun create(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val response = userService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar usuário",
        description = "Atualiza dados de um funcionário existente",
        responses = [
            ApiResponse(responseCode = "200", description = "Usuário atualizado"),
            ApiResponse(responseCode = "400", description = "Usuário não encontrado")
        ]
    )
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.update(id, request))
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Desativar usuário",
        description = "Soft delete - marca o usuário como inativo (active=false)",
        responses = [
            ApiResponse(responseCode = "204", description = "Usuário desativado"),
            ApiResponse(responseCode = "400", description = "Usuário não encontrado")
        ]
    )
    fun softDelete(@PathVariable id: UUID): ResponseEntity<Void> {
        userService.softDelete(id)
        return ResponseEntity.noContent().build()
    }
}
