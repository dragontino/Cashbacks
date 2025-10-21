package com.cashbacks.app

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import com.cashbacks.app.workers.CheckNewVersionWorker
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CheckNewVersionTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Rule
    fun permissionRule(): GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    @Test
    fun useCheckNewVersionWorker() {
        val worker = TestListenableWorkerBuilder<CheckNewVersionWorker>(context).build()
        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is` (ListenableWorker.Result.success()))

            val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            uiDevice.openNotification()

            Intents.init()
            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData("https://github.com/dragontino/Cashbacks/releases/download/v1.9.0/Cashbacks-1.9.0-release.apk")
                )
            )
            Intents.release()
        }
    }


    class WorkManagerIdlingResource(
        private val workManager: WorkManager,
        private val workId: UUID
    ) : IdlingResource {
        @Volatile
        private var callback: IdlingResource.ResourceCallback? = null

        override fun getName() = "WorkManagerIdlingResource"

        override fun isIdleNow(): Boolean {
            val info = workManager.getWorkInfoById(workId).get()
            val idle = info?.state?.isFinished
            println("Check is idle: $idle")
            if (idle == true) callback?.onTransitionToIdle()
            return idle == true
        }

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
            this.callback = callback
        }
    }
}