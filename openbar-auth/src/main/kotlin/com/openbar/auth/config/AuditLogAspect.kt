package com.openbar.auth.config

import com.openbar.auth.security.UserIdPrincipal
import com.openbar.auth.service.AuditLogService
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class AuditLogAspect(
    private val auditLogService: AuditLogService
) {

    @Pointcut("execution(* com.openbar.auth.web.controller.*.*(..))")
    fun controllerMethods() {}

    @Around("controllerMethods()")
    fun audit(joinPoint: ProceedingJoinPoint): Any {
        val request = getCurrentRequest()
        val endpoint = request?.requestURI ?: "unknown"
        val method = request?.method ?: "UNKNOWN"
        val ipAddress = getClientIp(request)
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = (authentication?.principal as? UserIdPrincipal)?.id
        val username = authentication?.name

        val action = determineAction(endpoint, method)

        return try {
            val result = joinPoint.proceed()
            auditLogService.record(
                userId = userId,
                username = username,
                action = action,
                details = "Success",
                endpoint = endpoint,
                method = method,
                ipAddress = ipAddress,
                success = true
            )
            result
        } catch (ex: Exception) {
            auditLogService.record(
                userId = userId,
                username = username,
                action = action,
                details = "Failed: ${ex.message}",
                endpoint = endpoint,
                method = method,
                ipAddress = ipAddress,
                success = false
            )
            throw ex
        }
    }

    private fun determineAction(endpoint: String, method: String): String {
        return when {
            endpoint.contains("/login") -> "LOGIN"
            endpoint.contains("/refresh") -> "TOKEN_REFRESH"
            endpoint.contains("/logout") -> "LOGOUT"
            endpoint.contains("/users") && method == "GET" -> "USER_LIST"
            endpoint.contains("/users") && method == "POST" -> "USER_CREATE"
            endpoint.contains("/users") && method == "PUT" -> "USER_UPDATE"
            endpoint.contains("/users") && method == "DELETE" -> "USER_DELETE"
            else -> "API_CALL"
        }
    }

    private fun getCurrentRequest(): HttpServletRequest? {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        return attributes?.request
    }

    private fun getClientIp(request: HttpServletRequest?): String? {
        if (request == null) return null
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",").first().trim()
        }
        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }
        return request.remoteAddr
    }
}
