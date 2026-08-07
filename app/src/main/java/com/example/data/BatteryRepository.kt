package com.example.data

import kotlinx.coroutines.flow.Flow

class BatteryRepository(private val batteryDao: BatteryDao) {
    val allConfigs: Flow<List<BatteryConfig>> = batteryDao.getAllConfigs()

    suspend fun insert(config: BatteryConfig) {
        batteryDao.insertConfig(config)
    }

    suspend fun deleteById(id: Int) {
        batteryDao.deleteConfigById(id)
    }
}
