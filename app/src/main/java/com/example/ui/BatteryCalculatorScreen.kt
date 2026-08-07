package com.example.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.BatteryConfig
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryCalculatorScreen(
    viewModel: BatteryViewModel,
    modifier: Modifier = Modifier
) {
    val savedConfigs by viewModel.savedConfigs.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Local UI states
    var activeTab by remember { mutableStateOf(0) } // 0: Devices & Load, 1: Battery Setup, 2: Solar Grid Guide, 3: Saved Packs, 4: LFP Pro Tools
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCopyrightDialog by remember { mutableStateOf(false) }
    var configNameInput by remember { mutableStateOf("") }
    val t = { key: String -> BatteryLocalizer.get(key, viewModel.appLanguage) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFFFA340),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t("app_title"),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLanguageDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Change Language",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showCopyrightDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Legal Notice & Copyright",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Dynamic Interactive Battery Gauge Header
            val totalKwh = viewModel.totalWattHours / 1000.0
            val usableKwh = viewModel.usableWattHours / 1000.0

            BatteryVisualHeader(
                seriesCells = viewModel.seriesCells,
                parallelCells = viewModel.parallelCells,
                totalCapacityAh = viewModel.packCapacityAh,
                nominalVoltage = viewModel.packNominalVoltage,
                totalKwh = totalKwh,
                usableKwh = usableKwh,
                dodPercent = viewModel.depthOfDischargePercent,
                cycleLife = viewModel.cycleLifeEstimate,
                cellName = viewModel.selectedCellChoice.modelName,
                cellForm = viewModel.selectedCellChoice.form,
                lang = viewModel.appLanguage
            )

            // Multi-tab Navigation Bar
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text(t("tab_load"), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text(t("tab_calc"), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text(t("tab_grid"), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.SolarPower, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text(t("tab_history"), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    text = { Text(t("tab_pro"), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tab Contents
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> ApplianceSelectionTab(
                        viewModel = viewModel,
                        onProceedToSetup = { activeTab = 1 }
                    )
                    1 -> CalculatorConfigTab(
                        viewModel = viewModel,
                        onSaveClicked = {
                            val formStr = if (viewModel.selectedCellChoice.form == LfpCellForm.CYLINDRICAL) "Cyl" else "Pris"
                            configNameInput = "${viewModel.seriesCells}S${viewModel.parallelCells}P (${formStr} ${viewModel.selectedCellChoice.capacityAh.roundToInt()}Ah)"
                            showSaveDialog = true
                        }
                    )
                    2 -> SolarGridGuideTab(viewModel = viewModel)
                    3 -> SavedSystemsTab(
                        savedConfigs = savedConfigs,
                        lang = viewModel.appLanguage,
                        onLoadConfig = { config ->
                            viewModel.loadConfig(config)
                            activeTab = 1 // Switch back to calculator
                        },
                        onDeleteConfig = { id -> viewModel.deleteConfig(id) }
                    )
                    4 -> LfpProToolsTab(
                        viewModel = viewModel,
                        lang = viewModel.appLanguage
                    )
                }
            }
        }
    }

    // Save Configuration Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(t("dialog_save_title"), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        t("dialog_save_desc"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = configNameInput,
                        onValueChange = { configNameInput = it },
                        label = { Text(t("dialog_input_label")) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_name_input"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (configNameInput.isNotBlank()) {
                            viewModel.saveCurrentConfig(configNameInput)
                            showSaveDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_config_confirm")
                ) {
                    Text(t("dialog_save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(t("dialog_cancel"))
                }
            }
        )
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(t("select_language_title"), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val languages = listOf(
                        "en" to "English",
                        "bn" to "বাংলা (Bengali)",
                        "zh" to "中文 (Chinese)",
                        "de" to "Deutsch (German)",
                        "ko" to "한국어 (Korean)"
                    )
                    languages.forEach { (code, name) ->
                        val isSelected = viewModel.appLanguage == code
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.appLanguage = code
                                    showLanguageDialog = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(t("dialog_cancel"))
                }
            }
        )
    }

    // Copyright & Legal Notice Dialog
    if (showCopyrightDialog) {
        AlertDialog(
            onDismissRequest = { showCopyrightDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "আইনগত সতর্কবার্তা ও কপিরাইট",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Legal Notice & Copyright",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Developer & Owner:",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tanzir Ahmed",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "GitHub: https://github.com/iamtanzir",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "এই সাইট, ডিজাইন ও কন্ট্রোল সিস্টেমের কোনো অংশ অনুমতি ছাড়া ক্লোন বা নকল করা সম্পূর্ণ বেআইনি। পাইরেসি করা হলে সাইবার সুরক্ষা আইন ও কপিরাইট আইনের অধীনে আইনি ব্যবস্থা নেয়া হবে।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    )

                    Text(
                        text = "Unauthorized cloning or replication of any part of this site, design, or control system will face direct legal action under Cyber Security & Copyright Laws of BD.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCopyrightDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("বুঝেছি (Close)")
                }
            }
        )
    }
}

@Composable
fun BatteryVisualHeader(
    seriesCells: Int,
    parallelCells: Int,
    totalCapacityAh: Double,
    nominalVoltage: Double,
    totalKwh: Double,
    usableKwh: Double,
    dodPercent: Double,
    cycleLife: Int,
    cellName: String,
    cellForm: LfpCellForm,
    lang: String
) {
    val t = { key: String -> BatteryLocalizer.get(key, lang) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info block
                Column {
                    Text(
                        text = t("pack_config"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${seriesCells}S ${parallelCells}P LFP Pack",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${nominalVoltage.format(1)} V | ${totalCapacityAh.roundToInt()} Ah Rated",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Energy details
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = t("cycle_life"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "~$cycleLife ${t("cycles")}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (dodPercent <= 80.0) BatteryFull else BatteryHalf
                    )
                    Text(
                        text = t("at_dod").replace("%s", dodPercent.roundToInt().toString()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Battery Cylinder Custom Drawing representing the DoD Usable energy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val level = dodPercent / 100.0
                    val animatedLevel by animateFloatAsState(
                        targetValue = level.toFloat(),
                        animationSpec = tween(600),
                        label = "BatteryLevelGauge"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 2.5.dp.toPx()
                        val capWidth = 10.dp.toPx()
                        val capHeight = 18.dp.toPx()

                        val mainBodyWidth = size.width - capWidth - 6.dp.toPx()
                        val mainBodyHeight = size.height

                        // 1. Draw Battery Outer Border Container (Main Body)
                        drawRoundRect(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            size = Size(mainBodyWidth, mainBodyHeight),
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                            style = Stroke(width = strokeWidth)
                        )

                        // 2. Draw Battery Positive Terminal Cap (Right Side)
                        drawRoundRect(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            topLeft = Offset(mainBodyWidth, (mainBodyHeight - capHeight) / 2),
                            size = Size(capWidth, capHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )

                        // 3. Draw Inner Fluid Fill representing the Configurable DoD Usable Energy
                        val innerPadding = 5.dp.toPx()
                        val maxFillWidth = mainBodyWidth - (innerPadding * 2)
                        val fillHeight = mainBodyHeight - (innerPadding * 2)

                        val fillWidth = maxFillWidth * animatedLevel

                        val brush = Brush.linearGradient(
                            colors = listOf(
                                GreenPrimaryLight,
                                BatteryFull
                            )
                        )

                        if (fillWidth > 0f) {
                            drawRoundRect(
                                brush = brush,
                                topLeft = Offset(innerPadding, innerPadding),
                                size = Size(fillWidth, fillHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }

                        // Draw lines showing depth limits (DoD threshold marker)
                        if (animatedLevel < 1f) {
                            val limitX = innerPadding + fillWidth
                            drawLine(
                                color = Color.Red.copy(alpha = 0.7f),
                                start = Offset(limitX, innerPadding),
                                end = Offset(limitX, innerPadding + fillHeight),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }

                    // Inside overlays showing energy metrics
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${t("usable")}: ${usableKwh.format(2)} kWh",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Usable Battery Capacity",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${t("total_energy")}: ${totalKwh.format(2)} kWh",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Total System Storage",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${t("cell_model")}: $cellName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${t("type")}: ${if (cellForm == LfpCellForm.CYLINDRICAL) t("cylindrical") else t("prismatic")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// TAB 1: Household Appliance selection and Load calculator
@Composable
fun ApplianceSelectionTab(
    viewModel: BatteryViewModel,
    onProceedToSetup: () -> Unit
) {
    val scrollState = rememberScrollState()
    val t = { key: String -> BatteryLocalizer.get(key, viewModel.appLanguage) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Subheader introducing the section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = t("connected_load"),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = t("connected_load_desc"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Global Target Backup Hours Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = t("backup_target"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${viewModel.targetBackupHours.roundToInt()} ${t("hours_label")}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Slider(
                    value = viewModel.targetBackupHours.toFloat(),
                    onValueChange = { viewModel.targetBackupHours = it.roundToInt().toDouble() },
                    valueRange = 1f..24f,
                    steps = 22,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(t("hour_1"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(t("hour_12"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(t("hour_24"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        // List of household appliances with Counter Controls
        Text(
            text = t("appliance_list_title"),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        viewModel.standardAppliances.forEach { appliance ->
            val quantity = viewModel.applianceQuantities[appliance.id] ?: 0
            val displayName = when (viewModel.appLanguage) {
                "bn" -> appliance.nameBn
                else -> appliance.nameEn
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (quantity > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                border = if (quantity > 0) borderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left side details
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Dynamic Material Icons
                        val iconVector = when (appliance.iconName) {
                            "lightbulb" -> Icons.Default.Lightbulb
                            "mode_fan" -> Icons.Default.Air
                            "tv" -> Icons.Default.Tv
                            "router" -> Icons.Default.Router
                            "kitchen" -> Icons.Default.Kitchen
                            "water" -> Icons.Default.WaterDrop
                            "ac_unit" -> Icons.Default.AcUnit
                            "laptop" -> Icons.Default.Laptop
                            "microwave" -> Icons.Default.SoupKitchen
                            else -> Icons.Default.Smartphone
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = displayName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${appliance.nominalWatts.toInt()} ${t("watts")} • ${t("surge")}: ${appliance.surgeMultiplier}x",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Right side counters
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.decrementAppliance(appliance.id) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrement", modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.widthIn(min = 20.dp),
                            textAlign = TextAlign.Center
                        )

                        IconButton(
                            onClick = { viewModel.incrementAppliance(appliance.id) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increment", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Dynamic Calculations Results Panel
        if (viewModel.totalRunningWatts > 0) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = t("pack_analysis"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(t("total_running_load"), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${viewModel.totalRunningWatts.toInt()} ${t("watts")}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(t("total_peak_load"), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${viewModel.peakSurgeWatts.toInt()} ${t("watts")}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE64A19),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(t("daily_energy_req"), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${viewModel.targetRequiredEnergyWh.toInt()} Wh (${(viewModel.targetRequiredEnergyWh / 1000.0).format(2)} kWh)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Status analysis compared with configured battery size
                    val batterySufficiency = viewModel.capacitySufficiencyStatus
                    val statusText = when (batterySufficiency) {
                        "OPTIMAL" -> when (viewModel.appLanguage) {
                            "bn" -> "অতিরিক্ত ব্যাকআপ ধারণক্ষমতা আছে! (Optimal & Safe)"
                            "zh" -> "有充足的备份容量！(安全推荐)"
                            "de" -> "Hervorragende Backup-Kapazität! (Optimal)"
                            "ko" -> "여유로운 백업 용량 확보! (안전 및 최적)"
                            else -> "Excellent Backup Capacity! (Optimal & Safe)"
                        }
                        "SUFFICIENT" -> when (viewModel.appLanguage) {
                            "bn" -> "প্যাক ধারণক্ষমতা পর্যাপ্ত! (Sufficient)"
                            "zh" -> "电池包容量充足！(充足)"
                            "de" -> "Pack-Kapazität ist ausreichend! (Sufficient)"
                            "ko" -> "배터리 팩 용량이 충분합니다! (충분)"
                            else -> "Pack Capacity is Sufficient! (Sufficient)"
                        }
                        "LOW" -> when (viewModel.appLanguage) {
                            "bn" -> "ব্যাটারি ধারণক্ষমতা কম (ব্যাকআপ সময় কমে যাবে)"
                            "zh" -> "电池容量较低 (备份时间将缩短)"
                            "de" -> "Geringe Akkukapazität (Backupzeit verkürzt sich)"
                            "ko" -> "배터리 용량 부족 (백업 시간이 단축됨)"
                            else -> "Low battery capacity (backup time will be shorter)"
                        }
                        else -> when (viewModel.appLanguage) {
                            "bn" -> "অপ্রতুল ব্যাটারি প্যাক! পর্যাপ্ত ব্যাকআপ পাবেন না।"
                            "zh" -> "电池包容量不足！无法满足目标备份。"
                            "de" -> "Ungenügende Akkukapazität! Ziel wird nicht erreicht."
                            "ko" -> "배터리 용량이 매우 부족합니다! 목표를 달성할 수 없습니다."
                            else -> "Insufficient battery pack! Won't meet target backup."
                        }
                    }

                    val statusColor = when (batterySufficiency) {
                        "OPTIMAL" -> BatteryFull
                        "SUFFICIENT" -> GreenPrimaryLight
                        "LOW" -> BatteryHalf
                        else -> BatteryLow
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (batterySufficiency == "OPTIMAL" || batterySufficiency == "SUFFICIENT") Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onProceedToSetup,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(t("btn_proceed"), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// TAB 2: Configurable LFP battery cells & pack details calculator
@Composable
fun CalculatorConfigTab(
    viewModel: BatteryViewModel,
    onSaveClicked: () -> Unit
) {
    val scrollState = rememberScrollState()
    val t = { key: String -> BatteryLocalizer.get(key, viewModel.appLanguage) }

    val baseLifeLabel = when (viewModel.appLanguage) {
        "bn" -> "বেস লাইফ"
        "zh" -> "基础寿命"
        "de" -> "Basislebensdauer"
        "ko" -> "기준 수명"
        else -> "Base Life"
    }
    val formLabel = when (viewModel.appLanguage) {
        "bn" -> "ফর্ম"
        "zh" -> "形态"
        "de" -> "Form"
        "ko" -> "형태"
        else -> "Form"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cell choice section (From 15Ah to 314Ah)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = t("select_lfp_cell"),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = t("select_lfp_cell_desc"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Grid selection of Cell choices
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.cellChoices.forEach { choice ->
                        val selected = viewModel.selectedCellChoice.id == choice.id
                        val cardBg = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        val border = if (selected) borderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else borderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardBg)
                                .border(border, RoundedCornerShape(8.dp))
                                .clickable { viewModel.applyCellChoice(choice.id) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { viewModel.applyCellChoice(choice.id) }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = choice.modelName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "$baseLifeLabel: ${choice.baseCycles} ${t("cycles")} • $formLabel: ${if (choice.form == LfpCellForm.CYLINDRICAL) t("cylindrical") else t("prismatic")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "${choice.capacityAh.toInt()} Ah",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Series count & Voltage configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = t("series_parallel"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${viewModel.seriesCells}S (${viewModel.packNominalVoltage.format(1)} V)",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = viewModel.seriesCells.toFloat(),
                    onValueChange = { viewModel.seriesCells = it.roundToInt() },
                    valueRange = 1f..32f,
                    steps = 30,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Standard S quick buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickVoltages = listOf(
                        4 to "12V (4S)",
                        8 to "24V (8S)",
                        16 to "48V (16S)"
                    )
                    quickVoltages.forEach { (sCount, label) ->
                        val selected = viewModel.seriesCells == sCount
                        SuggestionChip(
                            onClick = { viewModel.applySVoltPreset(sCount) },
                            label = { Text(label) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                labelColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Parallel count settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = t("parallel_count"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${viewModel.parallelCells}P (${viewModel.packCapacityAh.toInt()} Ah)",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = viewModel.parallelCells.toFloat(),
                    onValueChange = { viewModel.parallelCells = it.roundToInt() },
                    valueRange = 1f..16f,
                    steps = 14,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Discharge depth settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = t("depth_of_discharge"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${viewModel.depthOfDischargePercent.roundToInt()}%",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (viewModel.depthOfDischargePercent <= 80.0) BatteryFull else BatteryHalf,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    text = t("dod_desc"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Slider(
                    value = viewModel.depthOfDischargePercent.toFloat(),
                    onValueChange = { viewModel.depthOfDischargePercent = it.roundToInt().toDouble() },
                    valueRange = 10f..100f,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dops = listOf(80.0, 90.0, 100.0)
                    dops.forEach { dod ->
                        val selected = viewModel.depthOfDischargePercent == dod
                        val label = when (dod) {
                            80.0 -> when (viewModel.appLanguage) {
                                "bn" -> "80% (বেস্ট লাইফ)"
                                "zh" -> "80% (最佳寿命)"
                                "de" -> "80% (Beste Lebensdauer)"
                                "ko" -> "80% (최고 수명)"
                                else -> "80% (Best Life)"
                            }
                            90.0 -> when (viewModel.appLanguage) {
                                "bn" -> "90% (ভারসাম্য)"
                                "zh" -> "90% (平衡)"
                                "de" -> "90% (Ausgewogen)"
                                "ko" -> "90% (균형)"
                                else -> "90% (Balanced)"
                            }
                            else -> when (viewModel.appLanguage) {
                                "bn" -> "100% (সর্বোচ্চ রেঞ্জ)"
                                "zh" -> "100% (最大范围)"
                                "de" -> "100% (Max. Reichweite)"
                                "ko" -> "100% (최대 범위)"
                                else -> "100% (Max Range)"
                            }
                        }
                        SuggestionChip(
                            onClick = { viewModel.depthOfDischargePercent = dod },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                labelColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Save Battery Pack Button
        Button(
            onClick = onSaveClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_config_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(t("save_design_btn"), fontWeight = FontWeight.Bold)
        }
    }
}

// TAB 3: Solar installation advisor guidelines & Bills of materials
@Composable
fun SolarGridGuideTab(viewModel: BatteryViewModel) {
    val scrollState = rememberScrollState()
    val t = { key: String -> BatteryLocalizer.get(key, viewModel.appLanguage) }

    val advantagesLabel = when (viewModel.appLanguage) {
        "bn" -> "সুবিধাসমূহ (Advantages):"
        "zh" -> "优势 (Advantages):"
        "de" -> "Vorteile (Advantages):"
        "ko" -> "장점 (Advantages):"
        else -> "Advantages:"
    }
    val disadvantagesLabel = when (viewModel.appLanguage) {
        "bn" -> "অসুবিধাসমূহ (Disadvantages):"
        "zh" -> "劣势 (Disadvantages):"
        "de" -> "Nachteile (Disadvantages):"
        "ko" -> "단점 (Disadvantages):"
        else -> "Disadvantages:"
    }

    val offgridTitle = when (viewModel.appLanguage) {
        "bn" -> "🔋 অফ-গ্রিড সিস্টেম গাইডলাইন (Off-Grid System)"
        "zh" -> "🔋 离网系统指南 (Off-Grid System)"
        "de" -> "🔋 Off-Grid System Richtlinien"
        "ko" -> "🔋 오프그리드 시스템 가이드라인"
        else -> "🔋 Off-Grid System Guidelines"
    }
    val offgridDesc = when (viewModel.appLanguage) {
        "bn" -> "অফ-গ্রিড সোলার সিস্টেম গ্রিড বা জাতীয় বিদ্যুৎ লাইনের সাথে যুক্ত থাকে না। এটি সম্পূর্ণ স্বয়ংসম্পূর্ণ। দিনে সোলার ও রাতে ব্যাটারির মাধ্যমে লোড চালানো হয়।"
        "zh" -> "离网太阳能系统不连接电网，完全独立运行。白天由太阳能供电，夜间由电池组供电。"
        "de" -> "Off-Grid-Solarsysteme sind nicht an das Stromnetz angeschlossen. Sie sind autark. Tagsüber Solarstrom, nachts Akkubetrieb."
        "ko" -> "오프그리드 태양광 시스템은 전력망과 연결되지 않은 자립형 시스템입니다. 낮에는 태양광으로, 밤에는 배터리로 전력을 공급합니다."
        else -> "Off-grid solar systems are fully independent from the utility grid. Daytime loads run on solar, and nighttime is sustained by battery packs."
    }
    val offgridAdv1 = when (viewModel.appLanguage) {
        "bn" -> "জাতীয় গ্রিডের লোডশেডিং থেকে ১০০% মুক্তি।"
        "zh" -> "100% 摆脱电网停电的影响。"
        "de" -> "100% Unabhängigkeit von Netzausfällen."
        "ko" -> "전력망 정전으로부터 100% 자립."
        else -> "100% protection from utility grid load-shedding and power cuts."
    }
    val offgridAdv2 = when (viewModel.appLanguage) {
        "bn" -> "বিদ্যুৎ বিল সম্পূর্ণ শূন্য টাকায় নামিয়ে আনা সম্ভব।"
        "zh" -> "可以将电费降至零。"
        "de" -> "Stromkosten können komplett auf Null gesenkt werden."
        "ko" -> "전기 요금을 완전히 0원으로 줄일 수 있습니다."
        else -> "Completely eliminate monthly electricity utility bills."
    }
    val offgridAdv3 = when (viewModel.appLanguage) {
        "bn" -> "পাহাড়ি, গ্রামীণ বা প্রত্যন্ত অঞ্চলে যেখানে বিদ্যুৎ নেই সেখানে সেরা সমাধান।"
        "zh" -> "无电的山区、农村或偏远地区的最佳解决方案。"
        "de" -> "Ideale Lösung für abgelegene Gebiete ohne Netzanschluss."
        "ko" -> "전력망이 없는 오지, 산간 지역을 위한 최고의 솔루션."
        else -> "Best solution for remote, rural, or off-grid locations without grid access."
    }
    val offgridDis1 = when (viewModel.appLanguage) {
        "bn" -> "প্রাথমিক সেটআপ খরচ বেশি (ব্যাটারি কিনতে হয়)।"
        "zh" -> "初始安装成本高（需要购买电池组）。"
        "de" -> "Höhere Anfangsinvestition (Batteriespeicher erforderlich)."
        "ko" -> "초기 설치 비용이 높습니다 (배터리 팩 구매 필요)."
        else -> "High initial setup cost due to battery storage requirements."
    }
    val offgridDis2 = when (viewModel.appLanguage) {
        "bn" -> "টানা কয়েকদিন বৃষ্টি বা মেঘলা থাকলে ব্যাকআপে সমস্যা হতে পারে।"
        "zh" -> "连续阴雨天可能会影响后备电源。"
        "de" -> "Eingeschränkte Leistung bei tagelangem Regen oder Bewölkung."
        "ko" -> "장기간 흐리거나 비가 오면 백업 문제가 발생할 수 있습니다."
        else -> "Requires careful management during consecutive rainy or cloudy days."
    }

    val ongridTitle = when (viewModel.appLanguage) {
        "bn" -> "⚡ অন-গ্রিড নেট মিটারিং সিস্টেম (On-Grid System)"
        "zh" -> "⚡ 并网净计量系统 (On-Grid System)"
        "de" -> "⚡ On-Grid Net-Metering System"
        "ko" -> "⚡ 온그리드 계통연계형 시스템"
        else -> "⚡ On-Grid Net-Metering System"
    }
    val ongridDesc = when (viewModel.appLanguage) {
        "bn" -> "অন-গ্রিড সিস্টেম সরাসরি আপনার বাড়ির গ্রিড বা সরকারি বিদ্যুৎ লাইনের সাথে কাজ করে। দিনে উৎপাদিত অতিরিক্ত বিদ্যুৎ জাতীয় গ্রিডে রপ্তানি (Export) হয় নেট মিটারিংয়ের মাধ্যমে।"
        "zh" -> "并网系统直接与公共电网协同工作。白天产生的多余电力通过净计量出口回电网。"
        "de" -> "On-Grid-Systeme arbeiten direkt mit Ihrem Stromnetz zusammen. Überschüssige Energie wird über Net-Metering eingespeist."
        "ko" -> "계통연계형 시스템은 전력망과 연동하여 작동합니다. 낮 동안 생산된 여분의 전기는 계통선으로 역송출됩니다."
        else -> "On-grid systems operate in direct sync with the utility provider. Extra solar electricity generated during day is exported back via net-metering."
    }
    val ongridAdv1 = when (viewModel.appLanguage) {
        "bn" -> "সবচেয়ে কম খরচে সেটআপ করা যায় (ব্যাটারির প্রয়োজন হয় না)।"
        "zh" -> "最经济的设置成本（无需电池组）。"
        "de" -> "Kostengünstigste Installation (kein Akku erforderlich)."
        "ko" -> "설치 비용이 가장 저렴합니다 (배터리 불필요)."
        else -> "Most economical setup cost since no battery packs are needed."
    }
    val ongridAdv2 = when (viewModel.appLanguage) {
        "bn" -> "নেট মিটারিংয়ের মাধ্যমে মাস শেষে বিদ্যুৎ বিল উল্টো আয় বা মাইনাস হতে পারে।"
        "zh" -> "通过净计量实现零电费甚至售电盈利。"
        "de" -> "Ermöglicht negative Stromrechnungen durch Einspeisevergütung."
        "ko" -> "계통연계를 통해 전기 요금을 환급받거나 절감할 수 있습니다."
        else -> "Offsets utility bill, leading to negative bills or secondary income."
    }
    val ongridAdv3 = when (viewModel.appLanguage) {
        "bn" -> "ব্যাটারি ক্ষয়ের কোনো বাৎসরিক খরচ নেই।"
        "zh" -> "没有电池折旧和更换的年度维护成本。"
        "de" -> "Keine laufenden Kosten für Batterieverschleiß."
        "ko" -> "배터리 수명 저하 및 교체에 따른 비용이 없습니다."
        else -> "Zero recurrent cost of battery pack replacements over time."
    }
    val ongridDis1 = when (viewModel.appLanguage) {
        "bn" -> "জাতীয় গ্রিডে বিদ্যুৎ চলে গেলে বা লোডশেডিং হলে অন-গ্রিড সিস্টেমও বন্ধ হয়ে যায় (নিরাপত্তার স্বার্থে)।"
        "zh" -> "如果电网断电，出于安全考虑并网系统也会自动关闭。"
        "de" -> "Das System schaltet sich bei Stromausfällen im Netz aus Sicherheitsgründen ab."
        "ko" -> "전력망 정전 시 작업자 안전을 위해 시스템이 강제 종료됩니다."
        else -> "Shuts down automatically during power outages for utility crew safety."
    }
    val ongridDis2 = when (viewModel.appLanguage) {
        "bn" -> "কোনো প্রকার এমার্জেন্সি বিদ্যুৎ ব্যাকআপ বা স্টোরেজ থাকে না।"
        "zh" -> "不提供任何紧急电力备份或能量存储。"
        "de" -> "Keine Notstromversorgung oder Energiespeicherung vorhanden."
        "ko" -> "정전 시 비상용 백업 전원이나 ESS 장치가 없습니다."
        else -> "No emergency backup power or storage when grid goes offline."
    }

    val hybridTitle = when (viewModel.appLanguage) {
        "bn" -> "🔄 হাইব্রিড সিস্টেম গাইডলাইন (Hybrid System - Recommended)"
        "zh" -> "🔄 混合动力系统指南 (Hybrid System - 推荐)"
        "de" -> "🔄 Hybrid System (Empfohlen)"
        "ko" -> "🔄 하이브리드 시스템 (추천)"
        else -> "🔄 Hybrid System (Recommended)"
    }
    val hybridDesc = when (viewModel.appLanguage) {
        "bn" -> "হাইব্রিড সোলার সিস্টেম অন-গ্রিড এবং অফ-গ্রিড উভয়ের কম্বিনেশন। এটি জাতীয় গ্রিডের সাথে বিদ্যুৎ শেয়ার করতে পারে, আবার লোডশেডিংয়ে ব্যাটারি ব্যাকআপও নিশ্চিত করে।"
        "zh" -> "混合太阳能系统结合了并网和离网的优势。它可以与电网共享电力，并在停电时通过电池组提供备份。"
        "de" -> "Hybridsysteme kombinieren On-Grid und Off-Grid. Sie können Strom ins Netz einspeisen und bieten gleichzeitig Akku-Backup bei Netzausfall."
        "ko" -> "하이브리드 시스템은 계통연계형과 독립형의 혼합형입니다. 전력망과 전기를 공유하면서 정전 시 배터리 백업을 보장합니다."
        else -> "Hybrid solar systems combine the best of on-grid and off-grid. They share power with the grid, and ensure seamless battery backup during blackouts."
    }
    val hybridAdv1 = when (viewModel.appLanguage) {
        "bn" -> "লোডশেডিংয়ের সময় অটোমেটিক ব্যাকআপ চালু হয়।"
        "zh" -> "在电网断电时自动无缝切换到后备电源。"
        "de" -> "Automatische Umschaltung auf Notstrom bei Netzausfall."
        "ko" -> "정전 시 즉시 자동으로 비상 전원이 가동됩니다."
        else -> "Automated seamless transition to battery power during load-shedding."
    }
    val hybridAdv2 = when (viewModel.appLanguage) {
        "bn" -> "বাড়তি বিদ্যুৎ বিক্রি বা বিদ্যুৎ বিলে সর্বোচ্চ সাশ্রয় সম্ভব।"
        "zh" -> "最大限度地减少电费，并且可以向电网售电。"
        "de" -> "Maximale Einsparung und Option zur Einspeisung von Überschussstrom."
        "ko" -> "전력망 역송출 및 자가 소비로 전기 요금을 극대화하여 절감합니다."
        else -> "Achieves maximum financial savings along with net-metering eligibility."
    }
    val hybridAdv3 = when (viewModel.appLanguage) {
        "bn" -> "সবচেয়ে নির্ভরযোগ্য ও আধুনিক সৌর প্রযুক্তি।"
        "zh" -> "最可靠、最现代的太阳能技术解决方案。"
        "de" -> "Zuverlässigste und modernste Solartechnologie auf dem Markt."
        "ko" -> "가장 신뢰성 높고 현대적인 스마트 태양광 기술입니다."
        else -> "The most reliable and versatile solar technology available today."
    }
    val hybridDis1 = when (viewModel.appLanguage) {
        "bn" -> "সবচেয়ে ব্যয়বহুল সিস্টেম কারণ হাইব্রিড ইনভার্টার ও ব্যাটারি প্যাক উভয়ই কিনতে হয়।"
        "zh" -> "系统成本最高，因为需要同时购买混合逆变器和电池组。"
        "de" -> "Teuerstes System, da sowohl Hybrid-Wechselrichter als auch Speicher benötigt werden."
        "ko" -> "하이브리드 인버터와 배터리 팩을 모두 구매해야 하므로 가장 비용이 많이 듭니다."
        else -> "Highest upfront system cost as it requires both a hybrid inverter and battery pack."
    }

    val guideHeader = when (viewModel.appLanguage) {
        "bn" -> "🛠️ আপনার লোড অনুযায়ী সেটআপ গাইডলাইন"
        "zh" -> "🛠️ 根据您的负载推荐系统规格"
        "de" -> "🛠️ Systemspezifikationen für Ihre Last"
        "ko" -> "🛠️ 고객 부하 맞춤형 시스템 사양"
        else -> "🛠️ System Specs Recommended For Your Load"
    }
    val guideSubHeader = when (viewModel.appLanguage) {
        "bn" -> "আপনার মোট ${viewModel.totalRunningWatts.toInt()} ওয়াট লোডের ওপর ভিত্তি করে সোলার ইঞ্জিনিয়ারিং গাইড:"
        "zh" -> "基于您的总功率 ${viewModel.totalRunningWatts.toInt()} 瓦特的太阳能工程规格建议："
        "de" -> "Spezifikationen basierend auf Ihrer Gesamtlast von ${viewModel.totalRunningWatts.toInt()} W:"
        "ko" -> "총 부하 ${viewModel.totalRunningWatts.toInt()} W 기준 태양광 설계 사양 추천:"
        else -> "Engineering guidelines optimized for your total load of ${viewModel.totalRunningWatts.toInt()} Watts:"
    }
    val invTitle = when (viewModel.appLanguage) {
        "bn" -> "১. ইনভার্টার রেটিং (Inverter Sizing):"
        "zh" -> "1. 逆变器额定功率 (Inverter Sizing):"
        "de" -> "1. Wechselrichter-Dimensionierung (Inverter Sizing):"
        "ko" -> "1. 인버터 용량 설계 (Inverter Sizing):"
        else -> "1. Inverter Sizing & Rating:"
    }
    val invMinSize = when (viewModel.appLanguage) {
        "bn" -> "• ন্যূনতম সাইজ: ${viewModel.recommendedInverterWatts.roundToInt()} ওয়াট (Pure Sine Wave recommended)"
        "zh" -> "• 最小额定功率: ${viewModel.recommendedInverterWatts.roundToInt()} 瓦特 (推荐正弦波)"
        "de" -> "• Mindestkapazität: ${viewModel.recommendedInverterWatts.roundToInt()} W (Reine Sinuswelle empfohlen)"
        "ko" -> "• 최소 용량: ${viewModel.recommendedInverterWatts.roundToInt()} W (순수 정현파 추천)"
        else -> "• Minimum Capacity: ${viewModel.recommendedInverterWatts.roundToInt()} Watts (Pure Sine Wave recommended)"
    }
    val invSurgeSize = when (viewModel.appLanguage) {
        "bn" -> "• ন্যূনতম ১০ সে. সার্জ ক্ষমতা: ${viewModel.recommendedInverterSurgeWatts.roundToInt()} ওয়াট"
        "zh" -> "• 最小10秒浪涌功率: ${viewModel.recommendedInverterSurgeWatts.roundToInt()} 瓦特"
        "de" -> "• Mindest-Surge-Leistung (10s): ${viewModel.recommendedInverterSurgeWatts.roundToInt()} W"
        "ko" -> "• 최소 10초 서지 용량: ${viewModel.recommendedInverterSurgeWatts.roundToInt()} W"
        else -> "• Minimum 10s Surge Rating: ${viewModel.recommendedInverterSurgeWatts.roundToInt()} Watts"
    }
    val cableTitle = when (viewModel.appLanguage) {
        "bn" -> "২. ব্যাটারি ডিসি ক্যাবল সাইজ (Cabling & Safety):"
        "zh" -> "2. 电池直流电缆与安全 (Cabling & Safety):"
        "de" -> "2. Verkabelung & Sicherheit (Cabling & Safety):"
        "ko" -> "2. 배터리 DC 케이블 규격 및 안전 (Cabling & Safety):"
        else -> "2. Cabling & Safety Specifications:"
    }
    val maxCurrent = when (viewModel.appLanguage) {
        "bn" -> "• সর্বাধিক লোড কারেন্ট: ${viewModel.continuousRunningAmps.format(1)} Amperes"
        "zh" -> "• 最大连续负载电流: ${viewModel.continuousRunningAmps.format(1)} 安培"
        "de" -> "• Max. Dauerstrom: ${viewModel.continuousRunningAmps.format(1)} A"
        "ko" -> "• 최대 연속 부하 전류: ${viewModel.continuousRunningAmps.format(1)} A"
        else -> "• Maximum Continuous Current: ${viewModel.continuousRunningAmps.format(1)} A"
    }
    val startCurrent = when (viewModel.appLanguage) {
        "bn" -> "• স্টার্ট-আপ পিক কারেন্ট: ${viewModel.startingSurgeAmps.format(1)} Amperes"
        "zh" -> "• 启动峰值浪涌电流: ${viewModel.startingSurgeAmps.format(1)} 安培"
        "de" -> "• Anlaufstrom (Peak): ${viewModel.startingSurgeAmps.format(1)} A"
        "ko" -> "• 기동 피크 서지 전류: ${viewModel.startingSurgeAmps.format(1)} A"
        else -> "• Startup Peak Surge Current: ${viewModel.startingSurgeAmps.format(1)} A"
    }
    val recommendedCable = when (viewModel.appLanguage) {
        "bn" -> "• রিকমেন্ডেড ডিসি তার: ${viewModel.recommendedCableSize}"
        "zh" -> "• 推荐直流电缆规格: ${viewModel.recommendedCableSize}"
        "de" -> "• Empfohlenes DC-Kabel: ${viewModel.recommendedCableSize}"
        "ko" -> "• 권장 DC 케이블 규격: ${viewModel.recommendedCableSize}"
        else -> "• Recommended DC Cable: ${viewModel.recommendedCableSize}"
    }
    val recommendedFuse = when (viewModel.appLanguage) {
        "bn" -> "• রিকমেন্ডেড সেফটি ফিউজ: ${(viewModel.startingSurgeAmps * 1.3).roundToInt()} A (DC Fuse)"
        "zh" -> "• 推荐安全熔断器: ${(viewModel.startingSurgeAmps * 1.3).roundToInt()} A (直流熔断器)"
        "de" -> "• Empfohlene DC-Sicherung: ${(viewModel.startingSurgeAmps * 1.3).roundToInt()} A"
        "ko" -> "• 권장 DC 안전 퓨즈: ${(viewModel.startingSurgeAmps * 1.3).roundToInt()} A"
        else -> "• Recommended Safety Fuse: ${(viewModel.startingSurgeAmps * 1.3).roundToInt()} A (DC Fuse)"
    }
    val solarTitle = when (viewModel.appLanguage) {
        "bn" -> "৩. সোলার প্যানেল রিচার্জ গাইড (Solar Panel Sizing):"
        "zh" -> "3. 太阳能电池板匹配指南 (Solar Panel Sizing):"
        "de" -> "3. Solarmodul-Spezifikation (Solar Panel Sizing):"
        "ko" -> "3. 태양광 패널 용량 매칭 가이드 (Solar Panel Sizing):"
        else -> "3. Solar PV Array Panel Sizing:"
    }
    val solarDesc = when (viewModel.appLanguage) {
        "bn" -> "• আপনার ${viewModel.totalWattHours.toInt()} Wh ব্যাটারি ৫ ঘণ্টার রৌদ্রে পুরোপুরি রিচার্জ করতে ন্যূনতম ${viewModel.recommendedSolarWatts.roundToInt()} ওয়াটের প্যানেল অ্যারে লাগবে।"
        "zh" -> "• 在5小时有效光照下完全充满您的 ${viewModel.totalWattHours.toInt()} Wh 电池，至少需要 ${viewModel.recommendedSolarWatts.roundToInt()} 瓦特的太阳能电池板。"
        "de" -> "• Um Ihre ${viewModel.totalWattHours.toInt()} Wh Batterie in 5 Sonnenstunden vollständig aufzuladen, sind mindestens ${viewModel.recommendedSolarWatts.roundToInt()} W Solarmodule erforderlich."
        "ko" -> "• ${viewModel.totalWattHours.toInt()} Wh 배터리를 하루 5시간 일조 시간 기준 완충하기 위해서는 최소 ${viewModel.recommendedSolarWatts.roundToInt()} W 태양광 패널 어레이가 권장됩니다."
        else -> "• To fully recharge your ${viewModel.totalWattHours.toInt()} Wh battery bank in 5 peak sun hours, a solar PV array of at least ${viewModel.recommendedSolarWatts.roundToInt()} Watts is required."
    }
    val emptyWarning = when (viewModel.appLanguage) {
        "bn" -> "ডিভাইস সিলেক্ট করা থাকলে ইনভার্টার ও ক্যাবলের স্পেসিফিকেশন গাইডলাইন এখানে দেখতে পাবেন।"
        "zh" -> "选择家电负载后，此处将显示逆变器、电缆与配套辅料的推荐规格指南。"
        "de" -> "Wählen Sie Verbraucher aus, um hier Spezifikationen für Wechselrichter und Kabel zu sehen."
        "ko" -> "가전 부하를 선택하시면 인버터, DC 케이블 등의 추천 가이드라인이 여기에 표시됩니다."
        else -> "Select household appliances first to see custom inverter, cabling, and engineering specifications."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Grid Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = t("solar_grid_title"),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = t("select_grid_type"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val gridTypes = listOf(
                        "offgrid" to t("offgrid"),
                        "ongrid" to t("ongrid"),
                        "hybrid" to t("hybrid")
                    )
                    gridTypes.forEach { (type, label) ->
                        val selected = viewModel.selectedGridType == type
                        ElevatedFilterChip(
                            selected = selected,
                            onClick = { viewModel.selectedGridType = type },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Dynamic guidelines based on selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when (viewModel.selectedGridType) {
                    "offgrid" -> {
                        Text(
                            text = offgridTitle,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = offgridDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(advantagesLabel, fontWeight = FontWeight.Bold, color = BatteryFull)
                        BulletPoint(offgridAdv1)
                        BulletPoint(offgridAdv2)
                        BulletPoint(offgridAdv3)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(disadvantagesLabel, fontWeight = FontWeight.Bold, color = BatteryLow)
                        BulletPoint(offgridDis1)
                        BulletPoint(offgridDis2)
                    }
                    "ongrid" -> {
                        Text(
                            text = ongridTitle,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ongridDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(advantagesLabel, fontWeight = FontWeight.Bold, color = BatteryFull)
                        BulletPoint(ongridAdv1)
                        BulletPoint(ongridAdv2)
                        BulletPoint(ongridAdv3)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(disadvantagesLabel, fontWeight = FontWeight.Bold, color = BatteryLow)
                        BulletPoint(ongridDis1)
                        BulletPoint(ongridDis2)
                    }
                    "hybrid" -> {
                        Text(
                            text = hybridTitle,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = hybridDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(advantagesLabel, fontWeight = FontWeight.Bold, color = BatteryFull)
                        BulletPoint(hybridAdv1)
                        BulletPoint(hybridAdv2)
                        BulletPoint(hybridAdv3)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(disadvantagesLabel, fontWeight = FontWeight.Bold, color = BatteryLow)
                        BulletPoint(hybridDis1)
                    }
                }
            }
        }

        // Custom tailored design specifications panel
        if (viewModel.totalRunningWatts > 0) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = borderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = guideHeader,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = guideSubHeader,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = invTitle,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = invMinSize,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = invSurgeSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE64A19),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = cableTitle,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = maxCurrent,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = startCurrent,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = recommendedCable,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimaryLight
                    )
                    Text(
                        text = recommendedFuse,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (viewModel.selectedGridType != "ongrid") {
                        Text(
                            text = solarTitle,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = solarDesc,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = emptyWarning,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("• ", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helpers
fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

private fun Double.format(digits: Int): String = String.format(Locale.getDefault(), "%.${digits}f", this)


@Composable
fun DeveloperCopyrightSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified Owner",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "আইনগত সতর্কবার্তা ও কপিরাইট অধিকার",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Developer & Owner: Tanzir Ahmed",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "https://github.com/iamtanzir",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    .padding(vertical = 8.dp)
            )
            
            Text(
                text = "এই সাইট, ডিজাইন ও কন্ট্রোল সিস্টেমের কোনো অংশ অনুমতি ছাড়া ক্লোন বা নকল করা সম্পূর্ণ বেআইনি। পাইরেসি করা হলে সাইবার সুরক্ষা আইন ও কপিরাইট আইনের অধীনে আইনি ব্যবস্থা নেয়া হবে।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
            
            Text(
                text = "Unauthorized cloning or replication will face direct legal action under Cyber Security & Copyright Laws of BD.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


// TAB 4: Saved Systems History Tab
@Composable
fun SavedSystemsTab(
    savedConfigs: List<BatteryConfig>,
    lang: String,
    onLoadConfig: (BatteryConfig) -> Unit,
    onDeleteConfig: (Int) -> Unit
) {
    val t = { key: String -> BatteryLocalizer.get(key, lang) }
    val usableLabel = when (lang) {
        "bn" -> "ব্যবহারযোগ্য"
        "zh" -> "可用能量"
        "de" -> "Nutzbar"
        "ko" -> "가용 에너지"
        else -> "Usable"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (savedConfigs.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = t("no_saved"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = t("no_saved_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            item {
                Text(
                    text = t("saved_designs_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(savedConfigs, key = { it.id }) { config ->
                val nominalV = config.seriesCells * 3.2
                val totalKwh = (nominalV * config.cellCapacityAh) / 1000.0
                val usableKwh = totalKwh * (config.depthOfDischargePercent / 100.0)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLoadConfig(config) }
                        .testTag("saved_config_item_${config.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = config.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${config.seriesCells}S ${config.parallelCells}P • ${nominalV.format(1)}V • ${config.cellCapacityAh.toInt()}Ah Rated",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$usableLabel: ${usableKwh.format(2)} kWh (${config.depthOfDischargePercent.roundToInt()}% DoD)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = { onDeleteConfig(config.id) },
                            modifier = Modifier.testTag("delete_config_${config.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = t("delete_btn"),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Developer Copyright and Security Verification Banner
        item {
            DeveloperCopyrightSection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Composable
fun LfpProToolsTab(
    viewModel: BatteryViewModel,
    lang: String
) {
    val t = { key: String -> BatteryLocalizer.get(key, lang) }
    
    // Local state for interactive calculators
    var sCells by remember { mutableStateOf(viewModel.seriesCells) }
    var pCells by remember { mutableStateOf(viewModel.parallelCells) }
    var cellCapacityAh by remember { mutableStateOf(viewModel.selectedCellChoice.capacityAh) }
    var nominalCellVoltage by remember { mutableStateOf(3.2) } // LFP cell nominal is 3.2V

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = t("lfp_pro_tools_title"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // 1. LFP Energy Calculator
        LfpEnergyCalculatorCard(
            sCells = sCells,
            pCells = pCells,
            cellCapacityAh = cellCapacityAh,
            nominalCellVoltage = nominalCellVoltage,
            onSCellsChanged = { sCells = it },
            onPCellsChanged = { pCells = it },
            onCellCapacityChanged = { cellCapacityAh = it },
            onNominalVoltageChanged = { nominalCellVoltage = it },
            t = t
        )

        // 2. LFP Discharge Curve Chart
        LfpDischargeCurveCard(
            seriesCells = sCells,
            t = t
        )

        // 3. Unit Converter
        UnitConversionCard(
            t = t
        )
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}


@Composable
fun LfpEnergyCalculatorCard(
    sCells: Int,
    pCells: Int,
    cellCapacityAh: Double,
    nominalCellVoltage: Double,
    onSCellsChanged: (Int) -> Unit,
    onPCellsChanged: (Int) -> Unit,
    onCellCapacityChanged: (Double) -> Unit,
    onNominalVoltageChanged: (Double) -> Unit,
    t: (String) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = t("lfp_energy_calculator"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = t("lfp_energy_calc_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // 1. Cell Capacity Input
            Column {
                Text(
                    text = t("cell_capacity_ah") + ":",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = if (cellCapacityAh == 0.0) "" else cellCapacityAh.toString(),
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 0.0
                        onCellCapacityChanged(parsed)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text("Ah") },
                    colors = OutlinedTextFieldDefaults.colors()
                )
                
                // Presets row
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf(50.0, 100.0, 200.0, 280.0, 304.0)
                    presets.forEach { preset ->
                        val isSelected = cellCapacityAh == preset
                        SuggestionChip(
                            onClick = { onCellCapacityChanged(preset) },
                            label = { Text("${preset.toInt()} Ah") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // 2. Nominal Cell Voltage
            Column {
                Text(
                    text = t("nominal_voltage") + ":",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = if (nominalCellVoltage == 0.0) "" else nominalCellVoltage.toString(),
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 0.0
                        onNominalVoltageChanged(parsed)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text("V") }
                )
                
                // Quick presets
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val voltagePresets = listOf(
                        Triple(3.2, 1, "Cell (3.2V)"),
                        Triple(3.2, 4, "12V (4S)"),
                        Triple(3.2, 8, "24V (8S)"),
                        Triple(3.2, 16, "48V (16S)")
                    )
                    voltagePresets.forEach { preset ->
                        val isSelected = sCells == preset.second && nominalCellVoltage == preset.first
                        SuggestionChip(
                            onClick = {
                                onNominalVoltageChanged(preset.first)
                                onSCellsChanged(preset.second)
                            },
                            label = { Text(preset.third) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // 3. Series Count (S) Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = t("series_count"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${sCells}S",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = sCells.toFloat(),
                    onValueChange = { onSCellsChanged(it.roundToInt()) },
                    valueRange = 1f..16f,
                    steps = 14
                )
            }

            // 4. Parallel Count (P) Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = t("parallel_count"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${pCells}P",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = pCells.toFloat(),
                    onValueChange = { onPCellsChanged(it.roundToInt()) },
                    valueRange = 1f..10f,
                    steps = 8
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Spec Result Summary Box
            val packV = sCells * nominalCellVoltage
            val packAh = pCells * cellCapacityAh
            val packWh = packV * packAh
            val packKwh = packWh / 1000.0

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "LFP Pack Specifications & Energy:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = t("pack_voltage_total"), style = MaterialTheme.typography.bodyMedium)
                    Text(text = "${packV.format(1)} V", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = t("pack_capacity_total"), style = MaterialTheme.typography.bodyMedium)
                    Text(text = "${packAh.format(1)} Ah", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = t("total_energy_wh"), style = MaterialTheme.typography.bodyMedium)
                    Text(text = "${packWh.format(1)} Wh", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = t("total_energy_kwh"), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${packKwh.format(3)} kWh", 
                        fontWeight = FontWeight.ExtraBold, 
                        style = MaterialTheme.typography.titleMedium, 
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Lead-Acid comparison card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = t("lead_acid_comparison"),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = t("lead_acid_comp_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    
                    val laSizeAhNeeded = (packWh / 0.50) / packV
                    Text(
                        text = "${t("required_la_capacity")} ${laSizeAhNeeded.format(1)} Ah (at ${packV.format(1)}V)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


@Composable
fun LfpDischargeCurveCard(
    seriesCells: Int,
    t: (String) -> String
) {
    var touchSoC by remember { mutableStateOf(50f) }
    
    val cellV = getLfpCellVoltage(touchSoC.toDouble())
    val packV = cellV * seriesCells

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = t("discharge_curve_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = t("discharge_curve_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // The Interactive Canvas Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val graphLeft = 110f
                                    val graphRight = this@pointerInput.size.width - 40f
                                    val graphWidth = graphRight - graphLeft
                                    val relativeX = (offset.x - graphLeft).coerceIn(0f, graphWidth)
                                    val soc = 100f - (relativeX / graphWidth * 100f)
                                    touchSoC = soc.coerceIn(0f, 100f)
                                },
                                onDrag = { change, _ ->
                                    val graphLeft = 110f
                                    val graphRight = this@pointerInput.size.width - 40f
                                    val graphWidth = graphRight - graphLeft
                                    val relativeX = (change.position.x - graphLeft).coerceIn(0f, graphWidth)
                                    val soc = 100f - (relativeX / graphWidth * 100f)
                                    touchSoC = soc.coerceIn(0f, 100f)
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    
                    val graphLeft = 110f
                    val graphRight = width - 40f
                    val graphTop = 30f
                    val graphBottom = height - 60f
                    
                    val graphWidth = graphRight - graphLeft
                    val graphHeight = graphBottom - graphTop

                    // 1. Draw Grid Lines & Labels
                    val textPaint = Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                        isAntiAlias = true
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    }

                    // Vertical Grid Lines (SoC from 100% down to 0%)
                    val socSteps = listOf(100f, 80f, 60f, 40f, 20f, 0f)
                    socSteps.forEach { step ->
                        val gx = graphLeft + graphWidth * ((100f - step) / 100f)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.15f),
                            start = Offset(gx, graphTop),
                            end = Offset(gx, graphBottom),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "${step.roundToInt()}%",
                            gx - 25f,
                            graphBottom + 35f,
                            textPaint
                        )
                    }

                    // Horizontal Grid Lines (Voltage from 2.4V to 3.7V)
                    val voltSteps = listOf(2.5, 2.8, 3.0, 3.2, 3.4, 3.6)
                    voltSteps.forEach { step ->
                        val gy = graphBottom - graphHeight * ((step.toFloat() - 2.4f) / (3.7f - 2.4f))
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.15f),
                            start = Offset(graphLeft, gy),
                            end = Offset(graphRight, gy),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "${step}V",
                            15f,
                            gy + 8f,
                            textPaint
                        )
                    }

                    // 2. Draw Curve path
                    val curvePath = Path().apply {
                        var first = true
                        for (i in 100 downTo 0) {
                            val soc = i.toDouble()
                            val volt = getLfpCellVoltage(soc)
                            val cx = graphLeft + graphWidth * ((100f - soc.toFloat()) / 100f)
                            val cy = graphBottom - graphHeight * ((volt.toFloat() - 2.4f) / (3.7f - 2.4f))
                            if (first) {
                                moveTo(cx, cy)
                                first = false
                            } else {
                                lineTo(cx, cy)
                            }
                        }
                    }

                    // Draw filled Area gradient
                    val fillPath = Path().apply {
                        addPath(curvePath)
                        lineTo(graphRight, graphBottom)
                        lineTo(graphLeft, graphBottom)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF9800).copy(alpha = 0.25f),
                                Color(0xFFFF9800).copy(alpha = 0.0f)
                            ),
                            startY = graphTop,
                            endY = graphBottom
                        )
                    )

                    // Draw Stroke Path
                    drawPath(
                        path = curvePath,
                        color = Color(0xFFFF9800),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // 3. Draw Active touch interactive vertical crosshair and node
                    val activeX = graphLeft + graphWidth * ((100f - touchSoC) / 100f)
                    val activeY = graphBottom - graphHeight * ((cellV.toFloat() - 2.4f) / (3.7f - 2.4f))

                    // Vertical touch line
                    drawLine(
                        color = Color(0xFF03A9F4).copy(alpha = 0.6f),
                        start = Offset(activeX, graphTop),
                        end = Offset(activeX, graphBottom),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Pulsing Dot Outer
                    drawCircle(
                        color = Color(0xFF03A9F4).copy(alpha = 0.25f),
                        radius = 12.dp.toPx(),
                        center = Offset(activeX, activeY)
                    )

                    // Pulsing Dot Inner
                    drawCircle(
                        color = Color(0xFF03A9F4),
                        radius = 6.dp.toPx(),
                        center = Offset(activeX, activeY)
                    )
                }
            }

            // Real-time Values Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = t("soc_label"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${touchSoC.roundToInt()}%", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF03A9F4))
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = t("cell_volt_label"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${cellV.format(3)} V", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = t("pack_volt_label"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${packV.format(2)} V", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}


@Composable
fun UnitConversionCard(
    t: (String) -> String
) {
    var convertValueStr by remember { mutableStateOf("100") }
    var fromUnit by remember { mutableStateOf("Ah") }
    var toUnit by remember { mutableStateOf("kWh") }
    var voltageInputStr by remember { mutableStateOf("12.8") }

    var showFromMenu by remember { mutableStateOf(false) }
    var showToMenu by remember { mutableStateOf(false) }

    val units = listOf("Ah", "Wh", "kWh", "Joules", "MegaJoules", "BTU")

    val convertValue = convertValueStr.toDoubleOrNull() ?: 0.0
    val voltage = voltageInputStr.toDoubleOrNull() ?: 12.8

    val result = convertCapacity(convertValue, fromUnit, toUnit, voltage)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = t("unit_converter"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = t("unit_converter_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Value Input
            Column {
                Text(
                    text = t("convert_value"),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = convertValueStr,
                    onValueChange = { convertValueStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Dropdowns Side-by-Side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // From Unit selection
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = t("from_unit") + ":",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showFromMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(fromUnit)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = showFromMenu,
                            onDismissRequest = { showFromMenu = false },
                            modifier = Modifier.fillMaxWidth(0.42f)
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        fromUnit = unit
                                        showFromMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // To Unit selection
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = t("to_unit") + ":",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showToMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(toUnit)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = showToMenu,
                            onDismissRequest = { showToMenu = false },
                            modifier = Modifier.fillMaxWidth(0.42f)
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        toUnit = unit
                                        showToMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Show Voltage input if convert requires Ah calculation (physically voltage is necessary to relate Ah to Wh/Joules)
            if (fromUnit == "Ah" || toUnit == "Ah") {
                Column {
                    Text(
                        text = "System Nominal Voltage (V):",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = voltageInputStr,
                        onValueChange = { voltageInputStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { Text("V") },
                        singleLine = true
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Conversion result box
            val formattedResult = when (toUnit) {
                "kWh" -> result.format(4)
                "Joules" -> result.format(0)
                "MegaJoules" -> result.format(3)
                "BTU" -> result.format(2)
                else -> result.format(2)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = t("converted_result"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$convertValue $fromUnit = $formattedResult $toUnit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


// Unit Conversion Engine
fun convertCapacity(value: Double, fromUnit: String, toUnit: String, voltage: Double): Double {
    val whValue = when (fromUnit) {
        "Ah" -> value * voltage
        "Wh" -> value
        "kWh" -> value * 1000.0
        "Joules" -> value / 3600.0
        "MegaJoules" -> value * 277.7777777777778
        "BTU" -> value / 3.412142
        else -> value
    }
    
    return when (toUnit) {
        "Ah" -> whValue / voltage
        "Wh" -> whValue
        "kWh" -> whValue / 1000.0
        "Joules" -> whValue * 3600.0
        "MegaJoules" -> whValue / 277.7777777777778
        "BTU" -> whValue * 3.412142
        else -> whValue
    }
}


// Mathematical modeling of standard LiFePO4 (LFP) cell voltage vs SoC %
fun getLfpCellVoltage(soc: Double): Double {
    return when {
        soc >= 95.0 -> 3.30 + (soc - 95.0) * (3.45 - 3.30) / 5.0
        soc >= 90.0 -> 3.26 + (soc - 90.0) * (3.30 - 3.26) / 5.0
        soc >= 80.0 -> 3.24 + (soc - 80.0) * (3.26 - 3.24) / 10.0
        soc >= 50.0 -> 3.20 + (soc - 50.0) * (3.24 - 3.20) / 30.0
        soc >= 20.0 -> 3.15 + (soc - 20.0) * (3.20 - 3.15) / 30.0
        soc >= 10.0 -> 3.05 + (soc - 10.0) * (3.15 - 3.05) / 10.0
        soc >= 5.0 -> 2.85 + (soc - 5.0) * (3.05 - 2.85) / 5.0
        soc >= 2.0 -> 2.60 + (soc - 2.0) * (2.85 - 2.60) / 3.0
        soc >= 0.0 -> 2.50 + soc * (2.60 - 2.50) / 2.0
        else -> 2.50
    }
}
