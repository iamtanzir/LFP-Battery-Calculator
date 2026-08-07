# LFP Battery Calculator & Solar Hub

A modern, highly optimized Android application designed for solar enthusiasts, engineers, and DIY battery builders. It helps calculate, model, and manage Lithium Iron Phosphate (LiFePO4) battery packs, load configurations, and solar installations.

---

## 📱 Key Features

### 1. ⚡ LFP Pro Tools & Calculators
* **LFP Pack Spec & Energy Calculator:** Customize Series (S) and Parallel (P) configuration with custom nominal voltage and capacity inputs to determine total pack voltage, capacity, and overall energy in Wh and kWh.
* **Lead-Acid Equivalency Advantage:** Compare LFP packs with standard Lead-Acid counterparts under usable Depth of Discharge (DoD) values.
* **Interactive Discharge Curve Chart:** Visualizes the uniquely flat discharge plateau of LiFePO4 cells. Includes a responsive touch crosshair to display real-time State of Charge (SoC) vs. individual cell and overall pack voltages.
* **Capacity Unit Converter:** Quickly convert values across metrics including **Ah, Wh, kWh, Joules, MegaJoules, and BTUs** with real-time nominal voltage reference.

### 2. 🔌 Devices & Load Management
* Define device lists and calculate daily load requirements.
* Analyze dynamic backups based on real-time power consumption metrics.

### 3. 🔋 Custom Battery Setup
* Configure battery chemistry options, nominal system voltage parameters, and required backup hours.
* Auto-calculate suggested minimal battery storage size in Ah.

### 4. ☀️ Solar Grid & Off-Grid Guide
* Access optimized guides and wiring templates for off-grid PV architectures.
* Tips for sizing solar charge controllers (MPPT/PWM), inverters, and fuse ratings.

### 5. 💾 Saved Packs & Configuration History
* Safely store and organize your custom battery configuration models locally on the device.

---

## 🛠️ Technology Stack & Architecture

This application is built using modern Android development practices:

* **Jetpack Compose:** Declarative UI styled using Material Design 3 (M3) guidelines, featuring a dynamic light/dark theme, responsive canvas graphics, and adaptive layouts.
* **MVVM Architecture (Model-View-ViewModel):** Decoupled layers separating business logic from visual representations.
* **Room Database:** Secure, highly efficient local SQLite database mapping to Kotlin structures for pack storage and historical data persistence.
* **Localization & Multilingual Support:** Comprehensive support for **English (en)**, **Bengali (bn)**, and **Chinese (zh)** out-of-the-box.
* **StateFlow & Coroutines:** Native Kotlin asynchronous APIs ensuring smooth UI render cycles and non-blocking computations.

---

## 🚀 Getting Started (Build & Run)

### Prerequisites
* **Android SDK:** API level 26 (Android 8.0) or higher.
* **Kotlin:** 1.9.x
* **Gradle:** 8.x (Kotlin DSL)

### Compiling and Generating the APK
1. Open the project in your standard Android IDE or terminal environment.
2. Run the Gradle build task to generate the debug APK:
   ```bash
   gradle assembleDebug
   ```
3. Locate the compiled APK in:
   ```path
   app/build/outputs/apk/debug/app-debug.apk
   ```
4. Install the generated `.apk` directly onto your Android test phone or emulator.
