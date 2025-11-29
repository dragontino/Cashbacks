package com.cashbacks.core.network

import com.cashbacks.core.network.model.AppReleaseDto
import com.cashbacks.core.network.retrofit.AppVersionService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.measureTime

class RetrofitUnitTest {
    @Test
    fun appLatestRelease_isSuccess() {
        runBlocking {
            val appVersionService = AppVersionService()
            val release: AppReleaseDto
            val measuredTime = measureTime {
                release = appVersionService.getLatestRelease()
            }
            println("Measured time = $measuredTime")
            println("Release = $release")
            assertEquals(release.tagName, "v1.9.0")
        }
    }
}