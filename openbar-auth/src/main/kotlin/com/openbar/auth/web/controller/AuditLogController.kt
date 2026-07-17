package com.openbar.auth.web.controller

import com.openbar.auth.service.AuditLogService
import com.openbar.auth.web.dto.AuditLogResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth/audit-logs")
@Tag(name = "Audit Logs", description = "Registro de auditoria de ações")
class AuditLogController(
    private val auditLogService: AuditLogService
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Listar logs de auditoria",
        description = "Retorna lista paginada de logs de auditoria (apenas ADMIN)",
        responses = [
            ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            ApiResponse(responseCode = "403", description = "Acesso negado")
        ]
    )
    fun findAll(pageable: Pageable): ResponseEntity<Page<AuditLogResponse>> {
        return ResponseEntity.ok(auditLogService.findAll(pageable))
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Listar logs por usuário",
        description = "Retorna logs de auditoria de um usuário específico (apenas ADMIN)",
        responses = [
            ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            ApiResponse(responseCode = "403", description = "Acesso negado")
        ]
    )
    fun findByUserId(@PathVariable userId: UUID, pageable: Pageable): ResponseEntity<Page<AuditLogResponse>> {
        return ResponseEntity.ok(auditLogService.findByUserId(userId, pageable))
    }
}
