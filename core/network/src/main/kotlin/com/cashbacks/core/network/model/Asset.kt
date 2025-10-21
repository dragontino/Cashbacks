package com.cashbacks.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Asset(
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val url: String,
    val id: Int,
    val name: String,
    val label: String?,
    @SerialName("content_type") val contentType: String,
    val state: String,
    val size: Int,
    @SerialName("download_count") val downloadCount: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)