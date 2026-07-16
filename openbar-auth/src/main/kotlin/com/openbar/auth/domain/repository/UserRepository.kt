package com.openbar.auth.domain.repository

import com.openbar.auth.domain.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {

    fun findByUsername(username: String): Optional<User>

    fun existsByUsername(username: String): Boolean

    fun findByActiveTrue(pageable: Pageable): Page<User>
}
