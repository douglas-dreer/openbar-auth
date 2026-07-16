package com.openbar.auth.config

import com.openbar.auth.security.UserIdPrincipal
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class CustomPermissionEvaluator : PermissionEvaluator {

    override fun hasPermission(
        authentication: Authentication,
        targetDomainObject: Any?,
        permission: Any?
    ): Boolean {
        return false
    }

    override fun hasPermission(
        authentication: Authentication,
        targetId: Serializable?,
        targetType: String?,
        permission: Any?
    ): Boolean {
        if (targetType == "User" && permission == "own") {
            return canAccessOwnProfile(authentication, targetId)
        }
        return false
    }

    private fun canAccessOwnProfile(authentication: Authentication, targetId: Serializable?): Boolean {
        if (targetId == null) return false
        val principal = authentication.principal as? UserIdPrincipal ?: return false
        return principal.id.toString() == targetId.toString()
    }
}
