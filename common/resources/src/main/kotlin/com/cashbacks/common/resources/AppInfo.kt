package com.cashbacks.common.resources

import kotlinx.serialization.Serializable

@Serializable
data class AppInfo(
    val name: String,
    val version: String
)
