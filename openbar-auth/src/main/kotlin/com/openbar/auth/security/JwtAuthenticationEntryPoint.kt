package com.openbar.auth.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.net.URI

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {

    private val objectMapper = jacksonObjectMapper()

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val problem = mapOf(
            "type" to "about:blank",
            "title" to "Unauthorized",
            "status" to HttpStatus.UNAUTHORIZED.value(),
            "detail" to "Authentication required",
            "instance" to request.requestURI
        )

        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
        response.writer.write(objectMapper.writeValueAsString(problem))
    }
}
