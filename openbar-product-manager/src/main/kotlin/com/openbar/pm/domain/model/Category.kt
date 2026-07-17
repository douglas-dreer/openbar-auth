package com.openbar.pm.domain.model

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "categories")
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(length = 500)
    val description: String? = null,

    @Column(nullable = false)
    val active: Boolean = true
)
