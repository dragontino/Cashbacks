package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cashbacks.data.model.BankCardDB
import kotlinx.coroutines.flow.Flow

@Dao
interface CardsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBankCard(bankCardDB: BankCardDB): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateBankCard(bankCardDB: BankCardDB): Int

    @Query("SELECT * FROM Cards ORDER BY name ASC")
    fun fetchBankCards(): Flow<List<BankCardDB>>

    @Query(
        """
        SELECT * FROM Cards
        WHERE name LIKE '%' || :query || '%' OR number LIKE '%' || :query || '%' 
        OR paymentSystem LIKE '%' || :query || '%' OR holder LIKE '%' || :query || '%'
        OR validityPeriod LIKE '%' || :query || '%' OR comment LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    suspend fun searchBankCards(query: String): List<BankCardDB>

    @Query("SELECT * FROM Cards WHERE id = :id")
    suspend fun getBankCardById(id: Long): BankCardDB?

    @Delete
    suspend fun deleteBankCard(bankCardDB: BankCardDB): Int
}