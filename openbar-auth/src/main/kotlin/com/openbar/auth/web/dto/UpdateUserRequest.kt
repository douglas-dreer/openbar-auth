package com.openbar.auth.web.dto

import com.openbar.auth.domain.model.UserRole

data class UpdateUserRequest(
    val username: String?,
    val role: UserRole?,
    val active: Boolean?
)
