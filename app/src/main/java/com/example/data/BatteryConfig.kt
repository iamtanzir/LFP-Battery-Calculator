package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_configs")
data class BatteryConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val seriesCells: Int,
    val parallelCells: Int,
    val cellVoltage: Double,
    val cellCapacityAh: Double,
    val depthOfDischargePercent: Double,
    val timestamp: Long = System.currentTimeMillis()
)
