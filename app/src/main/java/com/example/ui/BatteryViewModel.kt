package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BatteryConfig
import com.example.data.BatteryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Cell chemistry type
enum class LfpCellForm {
    CYLINDRICAL,
    PRISMATIC
}

// Data class for standard premium LFP cell models
data class LfpCellChoice(
    val id: String,
    val modelName: String,
    val capacityAh: Double,
    val form: LfpCellForm,
    val baseCycles: Int // Base cycles at 80% DoD
)

// Data class for appliance configuration
data class Appliance(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val nominalWatts: Double,
    val surgeMultiplier: Double, // For peak start load (10 seconds)
    val iconName: String
)

class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BatteryRepository

    // Standard LFP cells in the market (Cylindrical and Prismatic 15Ah to 314Ah)
    val cellChoices = listOf(
        LfpCellChoice("cyl_6", "32700 Cylindrical (6 Ah)", 6.0, LfpCellForm.CYLINDRICAL, 2000),
        LfpCellChoice("cyl_15", "38120 Cylindrical (15 Ah)", 15.0, LfpCellForm.CYLINDRICAL, 2000),
        LfpCellChoice("cyl_15_2", "32140 Cylindrical (15 Ah)", 15.0, LfpCellForm.CYLINDRICAL, 2500),
        LfpCellChoice("cyl_20", "40135 Cylindrical (20 Ah)", 20.0, LfpCellForm.CYLINDRICAL, 2500),
        LfpCellChoice("pris_50", "Prismatic 50 Ah", 50.0, LfpCellForm.PRISMATIC, 4000),
        LfpCellChoice("pris_100", "Prismatic 100 Ah (Premium)", 100.0, LfpCellForm.PRISMATIC, 6000),
        LfpCellChoice("pris_200", "Prismatic 200 Ah (Premium)", 200.0, LfpCellForm.PRISMATIC, 6000),
        LfpCellChoice("pris_280", "Prismatic 280 Ah (Ultra)", 280.0, LfpCellForm.PRISMATIC, 8000),
        LfpCellChoice("pris_304", "Prismatic 304 Ah (Ultra)", 304.0, LfpCellForm.PRISMATIC, 8000),
        LfpCellChoice("pris_314", "Prismatic 314 Ah (NextGen)", 314.0, LfpCellForm.PRISMATIC, 8000)
    )

    // Standard household appliances
    val standardAppliances = listOf(
        Appliance("light", "LED Bulb", "এলইডি বাল্ব", 9.0, 1.0, "lightbulb"),
        Appliance("fan", "Ceiling Fan", "সিলিং ফ্যান", 75.0, 1.5, "mode_fan"),
        Appliance("ac", "Air Conditioner", "এয়ার কন্ডিশনার", 1500.0, 2.5, "ac_unit"),
        Appliance("tv", "LED Television", "এলইডি টিভি", 100.0, 1.2, "tv"),
        Appliance("fridge", "Refrigerator", "রেফ্রিজারেটর", 150.0, 3.0, "kitchen"),
        Appliance("pump", "Water Pump (1 HP)", "পানির পাম্প (১ ঘোড়া)", 750.0, 3.0, "water"),
        Appliance("desktop", "Computer Desktop", "কম্পিউটার ডেস্কটপ", 200.0, 1.2, "computer"),
        Appliance("mobile", "Smartphone Charger", "মোবাইল চার্জার", 15.0, 1.0, "smartphone"),
        Appliance("router", "Wifi Router", "ওয়াইফাই রাউটার", 10.0, 1.0, "router"),
        Appliance("iron", "Electric Iron", "ইলেকট্রিক ইস্ত্রি", 1000.0, 1.0, "iron")
    )

    // Current State Settings with SharedPreferences Auto-saving
    private val _selectedCellChoice = mutableStateOf(cellChoices[5])
    var selectedCellChoice: LfpCellChoice
        get() = _selectedCellChoice.value
        set(value) {
            _selectedCellChoice.value = value
            savePreferences()
        }

    private val _seriesCells = mutableStateOf(4)
    var seriesCells: Int
        get() = _seriesCells.value
        set(value) {
            _seriesCells.value = value
            savePreferences()
        }

    private val _parallelCells = mutableStateOf(1)
    var parallelCells: Int
        get() = _parallelCells.value
        set(value) {
            _parallelCells.value = value
            savePreferences()
        }

    private val _depthOfDischargePercent = mutableStateOf(80.0)
    var depthOfDischargePercent: Double
        get() = _depthOfDischargePercent.value
        set(value) {
            _depthOfDischargePercent.value = value
            savePreferences()
        }

    // Backup Requirement States
    private val _targetBackupHours = mutableStateOf(1.0)
    var targetBackupHours: Double
        get() = _targetBackupHours.value
        set(value) {
            _targetBackupHours.value = value
            savePreferences()
        }

    private val _selectedGridType = mutableStateOf("offgrid")
    var selectedGridType: String
        get() = _selectedGridType.value
        set(value) {
            _selectedGridType.value = value
            savePreferences()
        }

    private val _appLanguage = mutableStateOf("en")
    var appLanguage: String
        get() = _appLanguage.value
        set(value) {
            _appLanguage.value = value
            savePreferences()
        }

    // Map to keep quantities of selected appliances
    val applianceQuantities = mutableStateMapOf<String, Int>().apply {
        // Initialize default quantities
        standardAppliances.forEach { this[it.id] = 0 }
    }
    
    // Map to keep running hours of selected appliances
    val applianceHours = mutableStateMapOf<String, Int>().apply {
        standardAppliances.forEach { this[it.id] = 12 }
    }

    private var isPreferencesLoading = false

    private fun savePreferences() {
        if (isPreferencesLoading) return
        val sharedPrefs = getApplication<Application>().getSharedPreferences("lfp_energy_lab_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("selected_cell_id", selectedCellChoice.id)
            putInt("series_cells", seriesCells)
            putInt("parallel_cells", parallelCells)
            putFloat("dod_percent", depthOfDischargePercent.toFloat())
            putFloat("target_backup_hours", targetBackupHours.toFloat())
            putString("selected_grid_type", selectedGridType)
            putString("app_language", appLanguage)
            standardAppliances.forEach { appliance ->
                putInt("appliance_qty_${appliance.id}", applianceQuantities[appliance.id] ?: 0)
                putInt("appliance_hrs_${appliance.id}", applianceHours[appliance.id] ?: 12)
            }
            apply()
        }
    }

    private fun loadPreferences() {
        isPreferencesLoading = true
        try {
            val sharedPrefs = getApplication<Application>().getSharedPreferences("lfp_energy_lab_prefs", android.content.Context.MODE_PRIVATE)
            val savedCellId = sharedPrefs.getString("selected_cell_id", null)
            if (savedCellId != null) {
                val choice = cellChoices.find { it.id == savedCellId }
                if (choice != null) {
                    _selectedCellChoice.value = choice
                }
            }
            _seriesCells.value = sharedPrefs.getInt("series_cells", 4)
            _parallelCells.value = sharedPrefs.getInt("parallel_cells", 1)
            _depthOfDischargePercent.value = sharedPrefs.getFloat("dod_percent", 80.0f).toDouble()
            _targetBackupHours.value = sharedPrefs.getFloat("target_backup_hours", 1.0f).toDouble()
            _selectedGridType.value = sharedPrefs.getString("selected_grid_type", "offgrid") ?: "offgrid"
            _appLanguage.value = sharedPrefs.getString("app_language", "en") ?: "en"
            
            standardAppliances.forEach { appliance ->
                val defaultQty = 0
                applianceQuantities[appliance.id] = sharedPrefs.getInt("appliance_qty_${appliance.id}", defaultQty)
                applianceHours[appliance.id] = sharedPrefs.getInt("appliance_hrs_${appliance.id}", 12)
            }
        } finally {
            isPreferencesLoading = false
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BatteryRepository(database.batteryDao())
        loadPreferences()
    }

    // Saved Configurations Flow from Local SQLite Room Database
    val savedConfigs: StateFlow<List<BatteryConfig>> = repository.allConfigs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Computed Battery Metrics ---
    val packNominalVoltage: Double
        get() = seriesCells * 3.2

    val packCapacityAh: Double
        get() = parallelCells * selectedCellChoice.capacityAh

    val totalWattHours: Double
        get() = packNominalVoltage * packCapacityAh

    val usableWattHours: Double
        get() = totalWattHours * (depthOfDischargePercent / 100.0)

    val usableCapacityAh: Double
        get() = packCapacityAh * (depthOfDischargePercent / 100.0)

    // Automatically calculate cycle life based on Cell Form, Capacity, and DoD settings
    val cycleLifeEstimate: Int
        get() {
            val base = selectedCellChoice.baseCycles
            val dod = depthOfDischargePercent
            
            // Curve equation representing degradation acceleration at higher DoD values
            val multiplier = when {
                dod >= 100.0 -> 0.625
                dod >= 90.0 -> 0.625 + (100.0 - dod) * 0.0175 // 90% DoD has ~80% cycle life
                dod >= 80.0 -> 0.8 + (90.0 - dod) * 0.02 // 80% DoD has 100% cycle life
                dod >= 50.0 -> 1.0 + (80.0 - dod) * 0.02 // 50% DoD has ~160% cycle life
                dod >= 20.0 -> 1.6 + (50.0 - dod) * 0.03 // 20% DoD has ~250% cycle life
                else -> 2.5 + (20.0 - Math.max(0.0, dod)) * 0.05
            }
            return (base * multiplier).roundToInt()
        }

    // --- Computed Appliance Loads & Demands ---
    val totalRunningWatts: Double
        get() {
            return standardAppliances.sumOf { appliance ->
                val qty = applianceQuantities[appliance.id] ?: 0
                appliance.nominalWatts * qty
            }
        }

    val peakSurgeWatts: Double
        get() {
            return standardAppliances.sumOf { appliance ->
                val qty = applianceQuantities[appliance.id] ?: 0
                appliance.nominalWatts * appliance.surgeMultiplier * qty
            }
        }

    // Starting surge current in Amperes at the battery pack nominal voltage
    val startingSurgeAmps: Double
        get() {
            val volts = packNominalVoltage
            return if (volts > 0) peakSurgeWatts / volts else 0.0
        }

    // Calculated continuous backup current in Amperes
    val continuousRunningAmps: Double
        get() {
            val volts = packNominalVoltage
            return if (volts > 0) totalRunningWatts / volts else 0.0
        }

    // Calculated Energy target needed for the selected appliances over target backup hours (Wh)
    val targetRequiredEnergyWh: Double
        get() = totalRunningWatts * targetBackupHours

    // Target minimum battery capacity needed in Ampere hours (Ah) to sustain load safely
    val targetRequiredCapacityAh: Double
        get() {
            val volts = packNominalVoltage
            val dodFactor = depthOfDischargePercent / 100.0
            return if (volts > 0 && dodFactor > 0) (targetRequiredEnergyWh / volts) / dodFactor else 0.0
        }

    // Checks if the user's currently configured battery capacity is sufficient for their appliance load
    val capacitySufficiencyStatus: String
        get() {
            if (totalRunningWatts == 0.0) return "NO_LOAD"
            val deficitRatio = packCapacityAh / targetRequiredCapacityAh
            return when {
                deficitRatio >= 1.2 -> "OPTIMAL" // Battery is larger than needed
                deficitRatio >= 0.95 -> "SUFFICIENT" // Just enough
                deficitRatio >= 0.6 -> "LOW" // Underpowered but will run shorter
                else -> "INSUFFICIENT" // Severely underpowered
            }
        }

    // Minimum wire gauge guideline based on peak starting current to prevent heat buildup
    val recommendedCableSize: String
        get() {
            val amps = startingSurgeAmps
            return when {
                amps <= 25.0 -> "12 AWG (4 mm²) — For low-power small setups"
                amps <= 50.0 -> "10 AWG (6 mm²) — Recommended for light 12V loads"
                amps <= 90.0 -> "6 AWG (16 mm²) — Standard solar/battery cable"
                amps <= 150.0 -> "4 AWG (25 mm²) — High current thick copper wire"
                amps <= 250.0 -> "1/0 AWG (50 mm²) — Heavy duty inverter cable"
                else -> "2/0 AWG (70 mm²) or parallel runs — Ultra high capacity"
            }
        }

    // Recommended minimum inverter continuous rating with safety margins
    val recommendedInverterWatts: Double
        get() = (totalRunningWatts * 1.25).coerceAtLeast(300.0)

    // Recommended minimum inverter peak surge rating (must withstand startup surges)
    val recommendedInverterSurgeWatts: Double
        get() = peakSurgeWatts

    // Recommended solar array generation size in Watts to recharge the battery in 5 peak sun hours
    val recommendedSolarWatts: Double
        get() {
            // Include charge controller inefficiencies (approx 20% loss factor)
            return (totalWattHours * 1.2) / 5.0
        }

    // Preset configurations for easy setup
    fun applySVoltPreset(sCount: Int) {
        seriesCells = sCount
    }

    fun applyCellChoice(choiceId: String) {
        val choice = cellChoices.find { it.id == choiceId }
        if (choice != null) {
            selectedCellChoice = choice
        }
    }

    fun incrementAppliance(id: String) {
        val current = applianceQuantities[id] ?: 0
        if (current < 50) {
            applianceQuantities[id] = current + 1
            savePreferences()
        }
    }

    fun decrementAppliance(id: String) {
        val current = applianceQuantities[id] ?: 0
        if (current > 0) {
            applianceQuantities[id] = current - 1
            savePreferences()
        }
    }

    // Save current design to the database
    fun saveCurrentConfig(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insert(
                BatteryConfig(
                    name = name.trim(),
                    seriesCells = seriesCells,
                    parallelCells = parallelCells,
                    cellVoltage = 3.2,
                    cellCapacityAh = selectedCellChoice.capacityAh,
                    depthOfDischargePercent = depthOfDischargePercent
                )
            )
        }
    }

    // Delete a saved design
    fun deleteConfig(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    // Load a saved design
    fun loadConfig(config: BatteryConfig) {
        seriesCells = config.seriesCells
        parallelCells = config.parallelCells
        depthOfDischargePercent = config.depthOfDischargePercent
        
        // Match the closest cell choice by capacity
        val matchedChoice = cellChoices.find { it.capacityAh == config.cellCapacityAh }
            ?: cellChoices.find { it.capacityAh >= config.cellCapacityAh }
            ?: cellChoices.last()
        selectedCellChoice = matchedChoice
    }
}

class BatteryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BatteryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BatteryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
