package com.cashbacks.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Update
import com.cashbacks.core.database.PaymentSystemConverter
import com.cashbacks.core.database.entity.BankCardEntity
import com.cashbacks.core.database.entity.PrimaryBankCardEntity
import kotlinx.coroutines.flow.Flow

@TypeConverters(PaymentSystemConverter::class)
@Dao
interface CardsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBankCard(bankCardEntity: BankCardEntity): Long?

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateBankCard(bankCardEntity: BankCardEntity): Int

    @Query(
        """
            SELECT id, name, number, paymentSystem, holder, validityPeriod, cvv, maxCashbacksNumber
            FROM Cards 
            ORDER BY name, number ASC
        """
    )
    fun fetchAllBankCards(): Flow<List<PrimaryBankCardEntity>>

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
    suspend fun searchBankCards(query: String): List<PrimaryBankCardEntity>

    @Query("SELECT * FROM Cards WHERE id = :id")
    suspend fun getBankCardById(id: Long): BankCardEntity?

    @Query("SELECT * FROM Cards WHERE id = :id")
    fun fetchBankCardById(id: Long): Flow<BankCardEntity>

    @Query("DELETE FROM Cards WHERE id = :id")
    suspend fun deleteBankCardById(id: Long): Int
}