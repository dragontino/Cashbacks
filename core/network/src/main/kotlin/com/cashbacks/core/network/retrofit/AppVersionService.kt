package com.cashbacks.core.network.retrofit

import com.cashbacks.core.network.model.AppReleaseDto
import retrofit2.http.GET

internal interface AppVersionService {
    @GET("releases/latest")
    suspend fun getLatestRelease(): AppReleaseDto
}