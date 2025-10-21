package com.cashbacks.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Author(
    val login: String,
    val id: Int,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("events_url") val eventsUrl: String,
)