package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cashbacks.data.model.SettingsDB
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(entity = SettingsDB::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSettings(settings: SettingsDB): Long?

    @Query("SELECT COUNT(colorDesign) FROM Settings")
    suspend fun getRowCount(): Int

    @Update(entity = SettingsDB::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: SettingsDB): Int

    @Query("SELECT * FROM Settings")
    fun fetchSettings(): Flow<SettingsDB?>
}