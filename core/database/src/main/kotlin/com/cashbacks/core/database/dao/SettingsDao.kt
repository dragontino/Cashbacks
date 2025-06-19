package com.cashbacks.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cashbacks.core.database.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity): Long

    @Update(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun updateSettings(settings: SettingsEntity): Int

    @Query("SELECT * FROM Settings")
    fun fetchSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM Settings")
    suspend fun getSettings(): SettingsEntity?
}