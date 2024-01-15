package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cashbacks.data.model.SettingsDB
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(entity = SettingsDB::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSettings(settings: SettingsDB): Long?

    @Query("UPDATE Settings SET colorDesign = :colorDesign")
    suspend fun updateColorDesign(colorDesign: String): Int?

    @Query("UPDATE Settings SET dynamicColor = :dynamicColor")
    suspend fun updateDynamicColor(dynamicColor: Boolean): Int?

    @Query("SELECT * FROM Settings")
    fun fetchSettings(): Flow<SettingsDB?>
}