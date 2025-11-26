package com.cashbacks.core.network.retrofit

import com.cashbacks.core.network.BuildConfig
import com.cashbacks.core.network.model.AppReleaseDto
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

private const val BASE_URL = BuildConfig.APP_REPOS_URL

internal interface AppVersionService {
    @GET("releases/latest")
    suspend fun getLatestRelease(): AppReleaseDto
}

internal fun AppVersionService(): AppVersionService {
    val mediaType = "application/json; charset=UTF8".toMediaType()
    val json = Json { ignoreUnknownKeys = true }
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory(mediaType))
        .build()

    return retrofit.create(AppVersionService::class.java)
}