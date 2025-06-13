package com.cashbacks.features.category.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Long = 0L,
    val name: String = ""
)