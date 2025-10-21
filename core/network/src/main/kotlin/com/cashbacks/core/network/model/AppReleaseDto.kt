package com.cashbacks.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AppReleaseDto(
    val assets: List<Asset>,
    @SerialName("assets_url") val assetsUrl: String,
    val author: Author,
    val body: String,
    @SerialName("created_at") val createdAt: String,
    val draft: Boolean,
    @SerialName("html_url") val htmlUrl: String,
    val id: Int,
    val immutable: Boolean,
    val name: String,
    val prerelease: Boolean,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("tag_name") val tagName: String,
    @SerialName("updated_at") val updatedAt: String,
    val url: String,
)