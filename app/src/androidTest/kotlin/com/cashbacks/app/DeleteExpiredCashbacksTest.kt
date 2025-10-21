package com.cashbacks.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.cashbacks.app.workers.DeleteExpiredCashbacksWorker
import com.cashbacks.common.utils.now
import com.cashbacks.core.database.AppDatabase
import com.cashbacks.features.bankcard.domain.model.FullBankCard
import com.cashbacks.features.bankcard.domain.usecase.AddBankCardUseCase
import com.cashbacks.features.cashback.domain.model.BasicCashback
import com.cashbacks.features.cashback.domain.usecase.AddCashbackToCategoryUseCase
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.usecase.AddCategoryUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class DeleteExpiredCashbacksTest : KoinComponent {
    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()


        loadKoinModules(
            module {
                single { db }
            }
        )
    }

    @Test
    fun useDeleteExpiredCashbacksWorker() {
        val worker = TestListenableWorkerBuilder<DeleteExpiredCashbacksWorker>(context).build()
        runBlocking {
            addMockData()

            val result = worker.doWork()
            assertThat(result, `is` (ListenableWorker.Result.success()))
        }
    }

    private suspend fun addMockData() = coroutineScope {
        val addCategory = get<AddCategoryUseCase>()
        val addCashbackToCategory = get<AddCashbackToCategoryUseCase>()
        val addBankCard = get<AddBankCardUseCase>()


        val mockCategory = async {
            Category(id = 1, name = "ABC").also { addCategory(it) }
        }
        val mockBankCard = async {
            FullBankCard(id = 1).also { addBankCard(it) }
        }

        List(3) {
            val mockCashback = BasicCashback(
                id = it.toLong(),
                bankCard = mockBankCard.await(),
                amount = "69",
                expirationDate = LocalDate.now().minus(it, DateTimeUnit.DAY)
            )
            async {
                addCashbackToCategory(categoryId = mockCategory.await().id, cashback = mockCashback)
            }
        }.awaitAll()
    }


    @After
    fun tearDown() {
        db.close()
        stopKoin()
    }
}