package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import java.net.URLEncoder
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.roundToInt

val ZenerYellow = Color(0xFFFFD54F)
val ZenerDark = Color(0xFF121212)
val ZenerCardDark = Color(0xFF1E1E1E)
val ZenerTextLight = Color(0xFFE0E0E0)

fun getIcon(iconName: String) = when (iconName) {
    "lightbulb" -> Icons.Default.Lightbulb
    "mode_fan" -> Icons.Default.Air
    "tv" -> Icons.Default.Tv
    "router" -> Icons.Default.Router
    "kitchen" -> Icons.Default.Kitchen
    "water" -> Icons.Default.WaterDrop
    "ac_unit" -> Icons.Default.AcUnit
    "laptop" -> Icons.Default.Laptop
    "microwave" -> Icons.Default.SoupKitchen
    "computer" -> Icons.Default.DesktopWindows
    "iron" -> Icons.Default.Iron
    else -> Icons.Default.Smartphone
}

@Composable
fun ZenerSolarCalculatorWizard(viewModel: BatteryViewModel, t: (String) -> String) {
    var currentStep by remember { mutableStateOf(1) }
    
    // Step 2 State
    var sunlightHours by remember { mutableStateOf(5f) }
    var backupHours by remember { mutableStateOf(16f) }
    var systemType by remember { mutableStateOf("Hybrid / Off-Grid") }

    Column(modifier = Modifier.fillMaxSize().background(ZenerDark)) {
        // Header
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(color = ZenerYellow.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp), border = androidx.compose.foundation.BorderStroke(1.dp, ZenerYellow)) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = ZenerYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SMART SOLAR CALCULATOR", color = ZenerYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Solar System Calculator", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("সরাসরি আপনার ব্যবহৃত ফ্যান, লাইট এবং অন্যান্য লোডের বিবরণ দিয়ে প্রয়োজনীয় সোলার সিস্টেমের নিখুঁত পরিমাপ করুন।", color = ZenerTextLight, fontSize = 12.sp, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stepper
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                StepIndicator(step = 1, currentStep = currentStep, label = "LOAD")
                Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (currentStep >= 2) ZenerYellow else Color.DarkGray)
                StepIndicator(step = 2, currentStep = currentStep, label = "SIZING")
                Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (currentStep >= 3) ZenerYellow else Color.DarkGray)
                StepIndicator(step = 3, currentStep = currentStep, label = "RESULT")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Content Area
        Card(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = ZenerCardDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            when (currentStep) {
                1 -> ZenerLoadStep(viewModel) { currentStep = 2 }
                2 -> ZenerSizingStep(sunlightHours, { sunlightHours = it }, backupHours, { backupHours = it }, systemType, { systemType = it }, onBack = { currentStep = 1 }, onNext = { currentStep = 3 })
                3 -> ZenerResultStep(viewModel, sunlightHours, backupHours, systemType, onBack = { currentStep = 2 })
            }
        }
    }
}

@Composable
fun StepIndicator(step: Int, currentStep: Int, label: String) {
    val isActive = step <= currentStep
    val color = if (isActive) ZenerYellow else Color.DarkGray
    val textColor = if (isActive) Color.Black else Color.Gray
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(32.dp).background(color, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Text(step.toString(), color = textColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ZenerLoadStep(viewModel: BatteryViewModel, onNext: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp)) {
        Text("Quick Add Preset / লোড সিলেক্ট করুন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        val appliances = viewModel.standardAppliances
        appliances.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { app ->
                    val qty = viewModel.applianceQuantities[app.id] ?: 0
                    val isAdded = qty > 0
                    Card(
                        modifier = Modifier.weight(1f).height(64.dp).clickable {
                            viewModel.applianceQuantities[app.id] = qty + 1
                        },
                        colors = CardDefaults.cardColors(containerColor = if (isAdded) Color(0xFF2C2C2C) else ZenerDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isAdded) ZenerYellow else Color.DarkGray)
                    ) {
                        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(getIcon(app.iconName), contentDescription = null, tint = if(isAdded) ZenerYellow else Color.Gray, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.nameEn, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${app.nominalWatts.toInt()} Watts", color = ZenerTextLight, fontSize = 10.sp)
                            }
                            Icon(Icons.Default.Add, contentDescription = null, tint = ZenerYellow, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        val selectedCount = viewModel.applianceQuantities.values.count { it > 0 }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Your Load List ($selectedCount)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Customize specific quantities and running hours daily.", color = ZenerTextLight, fontSize = 12.sp)
            }
            if (selectedCount > 0) {
                TextButton(onClick = { viewModel.standardAppliances.forEach { viewModel.applianceQuantities[it.id] = 0 } }) {
                    Text("Clear All", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (selectedCount == 0) {
            Card(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = CardDefaults.cardColors(containerColor = ZenerDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
            ) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("আপনার লোড শিট ফাঁকা রয়েছে", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("উপরের প্যানেল থেকে আপনার ঘরের ফ্যান, লাইট বা এসি সিলেক্ট করে যোগ করুন।", color = ZenerTextLight, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        } else {
            viewModel.standardAppliances.forEach { app ->
                val qty = viewModel.applianceQuantities[app.id] ?: 0
                val hrs = viewModel.applianceHours[app.id] ?: 12
                if (qty > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = ZenerDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color(0xFF1E3A8A), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) {
                                Icon(getIcon(app.iconName), contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.padding(8.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.nameEn, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${app.nominalWatts.toInt()} W", color = ZenerTextLight, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFF2C2C2C), RoundedCornerShape(4.dp)).padding(4.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp).clickable { if(qty>1) viewModel.applianceQuantities[app.id] = qty - 1 })
                                Text(qty.toString(), color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp).clickable { viewModel.applianceQuantities[app.id] = qty + 1 })
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFF2C2C2C), RoundedCornerShape(4.dp)).padding(4.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp).clickable { if(hrs>1) viewModel.applianceHours[app.id] = hrs - 1 })
                                Text(hrs.toString(), color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp).clickable { if(hrs<24) viewModel.applianceHours[app.id] = hrs + 1 })
                            }
                            Text(" Hrs", color = ZenerTextLight, fontSize = 10.sp, modifier = Modifier.padding(start=4.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp).clickable { viewModel.applianceQuantities[app.id] = 0 })
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            var peakLoad = 0.0
            var dailyDemand = 0.0
            viewModel.standardAppliances.forEach { app ->
                val qty = viewModel.applianceQuantities[app.id] ?: 0
                val hrs = viewModel.applianceHours[app.id] ?: 12
                if (qty > 0) {
                    peakLoad += app.nominalWatts * qty
                    dailyDemand += app.nominalWatts * qty * hrs
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = ZenerDark), border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("PEAK SIZING LOAD", color = ZenerTextLight, fontSize = 10.sp)
                        Text("${peakLoad.toInt()} W", color = ZenerYellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = ZenerDark), border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DAILY ENERGY DEMAND", color = ZenerTextLight, fontSize = 10.sp)
                        Text(String.format("%.2f kWh", dailyDemand / 1000), color = ZenerYellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = ZenerYellow)) {
                Text("Step 2: Backup Preferences / সাইজিং হিসেব করুন →", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ZenerSizingStep(sunlight: Float, onSunlight: (Float) -> Unit, backup: Float, onBackup: (Float) -> Unit, type: String, onType: (String) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp)) {
        Text("Sunlight / দিনের বেলা ব্যবহার", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("সরাসরি সোলার থেকে দিনে কত ঘণ্টা চালাবেন?", color = ZenerTextLight, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ZenerDark), border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Daylight Running", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("${sunlight.toInt()} Hours", color = ZenerYellow, fontWeight = FontWeight.Bold)
                }
                Slider(value = sunlight, onValueChange = onSunlight, valueRange = 1f..10f, steps = 8, colors = SliderDefaults.colors(thumbColor = ZenerYellow, activeTrackColor = ZenerYellow))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Backup Hours / রাতের বেলার ব্যবহার", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("ব্যাটারি ব্যাকআপ থেকে রাতে কত ঘণ্টা চালাবেন?", color = ZenerTextLight, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ZenerDark), border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Battery Backup", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("${backup.toInt()} Hours", color = ZenerYellow, fontWeight = FontWeight.Bold)
                }
                Slider(value = backup, onValueChange = onBackup, valueRange = 1f..24f, steps = 22, colors = SliderDefaults.colors(thumbColor = ZenerYellow, activeTrackColor = ZenerYellow))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("System Type / সিস্টেম টাইপ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("আপনি কোন ধরনের সোলার সিস্টেম চাচ্ছেন?", color = ZenerTextLight, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        
        val types = listOf(
            "Hybrid (Off Grid) System (Battery Backup)",
            "Hybrid On-Grid System (Net Metering with Backup)",
            "Grid Tie System (Without Battery - Save Bill Only)",
            "Pump/Irrigation Solution"
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            types.forEach { t ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onType(t) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = type == t,
                        onClick = { onType(t) },
                        colors = RadioButtonDefaults.colors(selectedColor = ZenerYellow, unselectedColor = Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(t, color = if(type == t) Color.White else Color.Gray, fontWeight = if(type == t) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                Text("← Back")
            }
            Button(onClick = onNext, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = ZenerYellow)) {
                Text("Show Results →", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ZenerResultStep(viewModel: BatteryViewModel, sunlight: Float, backup: Float, type: String, onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp)) {
        var peakLoad = 0.0
        var dailyDemand = 0.0
        viewModel.standardAppliances.forEach { app ->
            val qty = viewModel.applianceQuantities[app.id] ?: 0
            val hrs = viewModel.applianceHours[app.id] ?: 12
            if (qty > 0) {
                peakLoad += app.nominalWatts * qty
                dailyDemand += app.nominalWatts * qty * hrs
            }
        }
        
        val inverterSizeStr = String.format("%.2f KW", (peakLoad * 1.25 / 1000.0).coerceAtLeast(1.0))
        val panelCapacityKwStr = String.format("%.2f KW", ((dailyDemand / 1000.0) / 4.0 * 1.2).coerceAtLeast(1.0))
        val batteryCapacityWh = peakLoad * backup
        val batteryAh = ceil(batteryCapacityWh / 48.0).toInt().coerceAtLeast(100)
        
        Text("Based on your requirements", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ZenerDark), border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Load (KW)", color = Color.Gray)
                    Text(String.format("%.2f KW", peakLoad/1000.0), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("System Type", color = Color.Gray)
                    Text(type.split(" (").first(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Daily Daylight Load Running", color = Color.Gray)
                    Text("${sunlight.toInt()} Hrs", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Daily Battery Backup System", color = Color.Gray)
                    Text("${backup.toInt()} Hrs", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Recommended Size:", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(inverterSizeStr, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ZenerDark.copy(alpha = 0.5f)), border = androidx.compose.foundation.BorderStroke(1.dp, ZenerYellow.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("দ্রষ্টব্য: সোলার পণ্যের দাম প্রতিনিয়ত পরিবর্তনশীল হওয়ায় সঠিক বাজেট জানতে কোটেশন সাবমিট করুন।", color = ZenerYellow, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                try {
                    val message = "Hello High Tech Company Bangladesh! I have calculated my solar requirements with the Solar Sizing App and would like to receive a custom quote.\n\n" +
                            "📋 *System Sizing Details:*\n" +
                            "• *Calculated Load:* ${String.format("%.2f KW", peakLoad/1000.0)}\n" +
                            "• *Recommended Capacity:* $inverterSizeStr\n" +
                            "• *System Type:* ${type.split(" (").first()}\n" +
                            "• *Daylight Run Time:* ${sunlight.toInt()} Hours\n" +
                            "• *Night Backup Time:* ${backup.toInt()} Hours\n" +
                            "• *Calculated Battery Bank:* ${batteryAh} Ah\n\n" +
                            "Please contact me back with the current quotation."
                    val encodedMessage = URLEncoder.encode(message, "UTF-8")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=8801556560580&text=$encodedMessage"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ZenerYellow)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Get Quotation on WhatsApp", color = Color.Black, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
            Text("← Back to Sizing")
        }
    }
}
