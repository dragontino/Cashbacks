package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import com.cashbacks.data.model.BankCardDB
import com.cashbacks.data.room.PaymentSystemConverter
import com.cashbacks.domain.model.BasicBankCard
import kotlinx.coroutines.flow.Flow

@Dao
interface CardsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBankCard(bankCardDB: BankCardDB): Long

    @Query("SELECT * FROM Cards ORDER BY id ASC")
    fun fetchBankCards(): Flow<List<BankCardDB>>


    @Query("SELECT * FROM Cards WHERE id = :id")
    suspend fun getBankCardById(id: Long) : BankCardDB

    @TypeConverters(PaymentSystemConverter::class)
    @Query("SELECT id, name, number, paymentSystem FROM Cards WHERE id = :id")
    suspend fun getBasicInfoAboutBankCardById(id: Long): BasicBankCard
}