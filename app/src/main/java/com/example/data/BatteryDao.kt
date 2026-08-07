package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryDao {
    @Query("SELECT * FROM battery_configs ORDER BY timestamp DESC")
    fun getAllConfigs(): Flow<List<BatteryConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: BatteryConfig)

    @Query("DELETE FROM battery_configs WHERE id = :id")
    suspend fun deleteConfigById(id: Int)
}
