package com.cashbacks.components.login.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginCredentials(
    val password: String
)
