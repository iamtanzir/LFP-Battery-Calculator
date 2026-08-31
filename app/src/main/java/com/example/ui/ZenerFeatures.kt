package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun ZenerSolarCalculatorTab(t: (String) -> String) {
    var monthlyBill by remember { mutableStateOf("") }
    var roofSpace by remember { mutableStateOf("") }
    var isOffGrid by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Text(t("zener_calc_title"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        
        Text(t("zener_calc_desc"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = monthlyBill,
                    onValueChange = { monthlyBill = it },
                    label = { Text(t("monthly_bill_bdt")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) }
                )
                
                OutlinedTextField(
                    value = roofSpace,
                    onValueChange = { roofSpace = it },
                    label = { Text(t("roof_space_sqft")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = isOffGrid, onCheckedChange = { isOffGrid = it })
                    Text(t("require_battery_backup"), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        
        // Results
        val bill = monthlyBill.toDoubleOrNull() ?: 0.0
        val space = roofSpace.toDoubleOrNull() ?: 0.0
        
        if (bill > 0) {
            // Rough calculation logic based on BD context
            // 1 kW system saves approx 1000-1500 BDT/month
            val recommendedKw = maxOf(1.0, ceil(bill / 1200.0))
            val panelsNeeded = ceil((recommendedKw * 1000) / 550.0).toInt()
            val spaceNeeded = panelsNeeded * 25 // approx 25 sqft per panel
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t("recommended_system"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    
                    ResultRow(t("system_size"), "${recommendedKw.roundToInt()} kW")
                    ResultRow(t("panels_required"), "$panelsNeeded x 550W")
                    ResultRow(t("space_required"), "$spaceNeeded sq.ft")
                    if (isOffGrid) {
                        val batteryAh = recommendedKw * 200 // arbitrary rule of thumb
                        ResultRow(t("battery_capacity"), "${batteryAh.toInt()} Ah (48V)")
                    }
                    val estPrice = if (isOffGrid) recommendedKw * 95000 else recommendedKw * 65000
                    ResultRow(t("est_cost"), "৳ ${String.format("%,d", estPrice.toLong())}")
                    
                    if (space > 0 && spaceNeeded > space) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(t("space_warning"), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ZenerPackagesTab(t: (String) -> String) {
    val packages = listOf(
        SolarPackage("1 kW Off-Grid System", "৳ 75,000", listOf("2 x 550W Solar Panels", "1 kVA Inverter", "1 x 100Ah Battery", "Installation & Accessories included")),
        SolarPackage("3 kW Off-Grid System", "৳ 2,20,000", listOf("6 x 550W Solar Panels", "3 kVA Inverter", "2 x 150Ah Battery", "Installation & Accessories included")),
        SolarPackage("5 kW Hybrid System", "৳ 4,50,000", listOf("10 x 550W Solar Panels", "5 kVA Hybrid Inverter", "4 x 200Ah Battery", "Net Metering Ready", "Installation & Accessories included")),
        SolarPackage("10 kW On-Grid System", "৳ 6,50,000", listOf("20 x 550W Solar Panels", "10 kW On-Grid Inverter", "Net Metering Supported", "No Battery (Grid Tied)", "Installation & Accessories included"))
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Text(t("zener_packages_title"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(t("zener_packages_desc"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(packages) { pkg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(pkg.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(pkg.price, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    pkg.features.forEach { feature ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(feature, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { /* Contact Sales */ }, modifier = Modifier.fillMaxWidth()) {
                        Text(t("request_quote"))
                    }
                }
            }
        }
    }
}

@Composable
fun ZenerProjectBuilderTab(t: (String) -> String) {
    var projectType by remember { mutableStateOf("Residential") }
    var roofType by remember { mutableStateOf("Flat Concrete") }
    var targetCapacity by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Architecture, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Text(t("zener_project_title"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        
        Text(t("zener_project_desc"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Project Type
                Column {
                    Text(t("project_type"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    val types = listOf("Residential", "Commercial", "Industrial", "Agricultural")
                    types.chunked(2).forEach { rowTypes ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowTypes.forEach { type ->
                                OutlinedButton(
                                    onClick = { projectType = type },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (projectType == type) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    )
                                ) {
                                    Text(type, textAlign = TextAlign.Center, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Roof Type
                Column {
                    Text(t("roof_type"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    val rTypes = listOf("Flat Concrete", "Tin Shed", "Tiles", "Ground Mount")
                    rTypes.chunked(2).forEach { rowTypes ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowTypes.forEach { type ->
                                OutlinedButton(
                                    onClick = { roofType = type },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (roofType == type) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    )
                                ) {
                                    Text(type, textAlign = TextAlign.Center, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Target Capacity
                OutlinedTextField(
                    value = targetCapacity,
                    onValueChange = { targetCapacity = it },
                    label = { Text(t("target_capacity_kw")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(t("submit_project_req"))
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(t("req_submitted")) },
            text = { Text(t("req_submitted_desc")) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

data class SolarPackage(val name: String, val price: String, val features: List<String>)
