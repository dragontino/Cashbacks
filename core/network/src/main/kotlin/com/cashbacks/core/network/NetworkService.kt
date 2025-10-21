package com.cashbacks.core.network

import com.cashbacks.core.network.model.AppReleaseDto
import com.cashbacks.core.network.retrofit.AppVersionService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

sealed interface NetworkService {
    suspend fun getAppLatestVersion(): String

    suspend fun getAppDownloadLink(): String?

    suspend fun getReleaseUrl(): String
}


internal class NetworkServiceImpl(
    private val appVersionService: AppVersionService,
    private val dispatcher: CoroutineDispatcher
) : NetworkService {
    private var cachedRelease: AppReleaseDto? = null
    private var cachedTimeMillis: Long = System.currentTimeMillis()

    override suspend fun getAppLatestVersion(): String = withContext(dispatcher) {
        val versionTag = getLatestRelease().tagName
        return@withContext versionTag.removePrefix("v")
    }

    override suspend fun getAppDownloadLink(): String? = withContext(dispatcher) {
        val release = getLatestRelease()
        val apkAsset = release.assets
            .find { it.contentType == "application/vnd.android.package-archive" }
            ?: release.assets.firstOrNull()
        return@withContext apkAsset?.browserDownloadUrl
    }

    override suspend fun getReleaseUrl(): String {
        return getLatestRelease().htmlUrl
    }

    private suspend fun getLatestRelease(): AppReleaseDto {
        cachedRelease?.let { release ->
            if (System.currentTimeMillis() - cachedTimeMillis < 24 * 60 * 60 * 1000) {
                return release
            }
        }
        val release = appVersionService.getLatestRelease()
        cachedRelease = release
        cachedTimeMillis = System.currentTimeMillis()
        return release
    }
}