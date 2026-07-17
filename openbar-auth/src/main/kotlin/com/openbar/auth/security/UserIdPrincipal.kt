package com.openbar.auth.security

import java.util.UUID

data class UserIdPrincipal(val id: UUID) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserIdPrincipal) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = id.toString()
}
