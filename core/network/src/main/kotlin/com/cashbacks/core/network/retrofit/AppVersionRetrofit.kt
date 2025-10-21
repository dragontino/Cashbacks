package com.cashbacks.core.network.retrofit

import com.cashbacks.core.network.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

internal object AppVersionRetrofit {
    const val BASE_URL: String = BuildConfig.APP_REPOS_URL
}

internal fun buildAppRepositoryRetrofit(): Retrofit {
    val mediaType = "application/json; charset=UTF8".toMediaType()
    val json = Json { ignoreUnknownKeys = true }
    return Retrofit.Builder()
        .baseUrl(AppVersionRetrofit.BASE_URL)
        .addConverterFactory(json.asConverterFactory(mediaType))
        .build()
}