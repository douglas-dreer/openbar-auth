package com.openbar.auth.service

import com.openbar.auth.domain.model.User
import com.openbar.auth.domain.repository.UserRepository
import com.openbar.auth.web.dto.CreateUserRequest
import com.openbar.auth.web.dto.UpdateUserRequest
import com.openbar.auth.web.dto.UserResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun findAll(pageable: Pageable): Page<UserResponse> {
        return userRepository.findByActiveTrue(pageable).map { it.toResponse() }
    }

    fun findById(id: UUID): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found with id: $id") }
        return user.toResponse()
    }

    fun create(request: CreateUserRequest): UserResponse {
        require(!userRepository.existsByUsername(request.username)) { "Username already exists: ${request.username}" }

        val user = User(
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            role = request.role
        )

        return userRepository.save(user).toResponse()
    }

    fun update(id: UUID, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found with id: $id") }

        val updated = user.copy(
            username = request.username ?: user.username,
            role = request.role ?: user.role,
            active = request.active ?: user.active
        )

        return userRepository.save(updated).toResponse()
    }

    fun softDelete(id: UUID) {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found with id: $id") }

        userRepository.save(user.copy(active = false))
    }

    private fun User.toResponse() = UserResponse(
        id = id!!,
        username = username,
        role = role,
        active = active
    )
}
