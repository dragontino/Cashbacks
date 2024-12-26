package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cashbacks.data.model.BankCardDB
import com.cashbacks.data.model.PrimaryBankCardDB
import kotlinx.coroutines.flow.Flow

@Dao
interface CardsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBankCard(bankCardDB: BankCardDB): Long?

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateBankCard(bankCardDB: BankCardDB): Int

    @Query(
        """
            SELECT id, name, number, paymentSystem, holder, validityPeriod, cvv, maxCashbacksNumber
            FROM Cards 
            ORDER BY name, number ASC
        """
    )
    fun fetchAllBankCards(): Flow<List<PrimaryBankCardDB>>

    @Query(
        """
            SELECT id, name, number, paymentSystem, holder, validityPeriod, cvv, maxCashbacksNumber
            FROM Cards
            WHERE name LIKE '%' || :query || '%' OR number LIKE '%' || :query || '%'
                OR paymentSystem LIKE '%' || :query || '%' OR holder LIKE '%' || :query || '%'
                OR validityPeriod LIKE '%' || :query || '%' OR comment LIKE '%' || :query || '%'
                OR maxCashbacksNumber LIKE '%' || :query || '%' 
            ORDER BY name, number ASC
        """
    )
    suspend fun searchBankCards(query: String): List<PrimaryBankCardDB>

    @Query("SELECT * FROM Cards WHERE id = :id")
    suspend fun getBankCardById(id: Long): BankCardDB?

    @Query("SELECT * FROM Cards WHERE id = :id")
    fun fetchBankCardById(id: Long): Flow<BankCardDB>

    @Query("DELETE FROM Cards WHERE id = :id")
    suspend fun deleteBankCardById(id: Long): Int
}