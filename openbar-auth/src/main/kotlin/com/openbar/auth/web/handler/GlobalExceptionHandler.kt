package com.openbar.auth.web.handler

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ProblemDetail> {
        val problem = ProblemDetail(
            type = URI("about:blank"),
            title = "Bad Request",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = ex.message,
            instance = URI("/api/v1/auth")
        )
        return ResponseEntity.badRequest().body(problem)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ProblemDetail> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { error: FieldError ->
            "${error.field}: ${error.defaultMessage}"
        }

        val problem = ProblemDetail(
            type = URI("about:blank"),
            title = "Validation Error",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = errors,
            instance = URI("/api/v1/auth")
        )
        return ResponseEntity.badRequest().body(problem)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(@Suppress("UNUSED_PARAMETER") ex: Exception): ResponseEntity<ProblemDetail> {
        val problem = ProblemDetail(
            type = URI("about:blank"),
            title = "Internal Server Error",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            detail = "An unexpected error occurred",
            instance = URI("/api/v1/auth")
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem)
    }
}

data class ProblemDetail(
    val type: URI,
    val title: String,
    val status: Int,
    val detail: String?,
    val instance: URI,
    val timestamp: Instant = Instant.now()
)
