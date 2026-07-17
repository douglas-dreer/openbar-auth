package com.openbar.auth.security

import com.openbar.auth.service.JwtBlacklistService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtBlacklistService: JwtBlacklistService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractToken(request)

            if (token != null && jwtTokenProvider.validateToken(token)) {
                val jti = jwtTokenProvider.getJtiFromToken(token)

                if (jwtBlacklistService.isTokenBlacklisted(jti)) {
                    log.debug("Token is blacklisted: $jti")
                } else {
                    val userId = jwtTokenProvider.getUserIdFromToken(token)
                    val role = jwtTokenProvider.getRoleFromToken(token)

                    val principal = UserIdPrincipal(UUID.fromString(userId))
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))

                    val authentication = UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                    )

                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (e: Exception) {
            log.debug("Could not set authentication: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization") ?: return null
        return if (bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}
