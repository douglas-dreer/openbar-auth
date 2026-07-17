package com.openbar.pm.domain.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val name: String,

    @Column(length = 1000)
    val description: String? = null,

    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: Category,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val routing: Routing,

    @Column(nullable = false)
    val active: Boolean = true
)
