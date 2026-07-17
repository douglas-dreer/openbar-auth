package com.openbar.auth.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.ConsumptionProbe
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class RateLimitFilter(
    @Value("\${rate-limit.enabled:true}")
    private val enabled: Boolean,
    @Value("\${rate-limit.max-requests:5}")
    private val maxRequests: Long,
    @Value("\${rate-limit.period-minutes:1}")
    private val periodMinutes: Long
) : OncePerRequestFilter() {

    private val objectMapper = jacksonObjectMapper()
    private val buckets = ConcurrentHashMap<String, Bucket>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!enabled || !isLoginRequest(request)) {
            filterChain.doFilter(request, response)
            return
        }

        val clientIp = getClientIp(request)
        val bucket = buckets.computeIfAbsent(clientIp) {
            Bucket.builder()
                .addLimit(
                    Bandwidth.builder()
                        .capacity(maxRequests)
                        .refillGreedy(maxRequests, Duration.ofMinutes(periodMinutes))
                        .build()
                )
                .build()
        }

        val probe = bucket.tryConsumeAndReturnRemaining(1)

        if (probe.isConsumed) {
            response.addHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())
            filterChain.doFilter(request, response)
        } else {
            val waitTimeNanos = probe.nanosToWaitForRefill
            val waitTimeSeconds = TimeUnit.NANOSECONDS.toSeconds(waitTimeNanos)

            response.addHeader("X-Rate-Limit-Retry-After-Seconds", waitTimeSeconds.toString())

            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE

            val problem = mapOf(
                "type" to "about:blank",
                "title" to "Too Many Requests",
                "status" to 429,
                "detail" to "Rate limit exceeded. Try again in $waitTimeSeconds seconds."
            )
            response.writer.write(objectMapper.writeValueAsString(problem))
        }
    }

    private fun isLoginRequest(request: HttpServletRequest): Boolean {
        return request.requestURI == "/api/v1/auth/login" &&
            request.method.equals("POST", ignoreCase = true)
    }

    private fun getClientIp(request: HttpServletRequest): String {
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
