# Zener BD Solar Calculator

A modern, highly optimized Android application designed for calculating solar requirements, modeling battery systems, and generating project quotations, styled after the Zener BD calculator.

## 📱 Key Features

### 1. ☀️ Smart Solar Calculator Wizard
* **Step 1: Load Sizing:** Add standard appliances (Fans, ACs, Lights, Pumps) and configure quantities and daily running hours. Automatically calculates Peak Sizing Load and Daily Energy Demand.
* **Step 2: Backup Preferences:** Configure daily daylight running hours, nighttime battery backup requirements, and select the system type (Hybrid Off-Grid, Net Metering, etc.).
* **Step 3: Result & Quotation:** Instant generation of system recommendations, including Inverter Capacity, Solar Panel KW, Battery Bank Ah, and an estimated total budget.

### 2. 📦 Ready Solar Packages
* Browse pre-configured solar packages tailored for residential and commercial needs.
* View included features, total pricing, and request custom installations.

### 3. 🏗️ Project Builder
* A complete quotation request form for custom installations.
* Input roof types, target capacity, and project type to directly connect with engineering teams.

## 🛠️ Technology Stack & Architecture

This application is built using modern Android development practices:
* **Jetpack Compose:** Declarative UI styled with Material Design 3 guidelines, enforcing a custom dark/yellow Zener brand theme.
* **MVVM Architecture:** Clean separation of business logic and UI state via Kotlin ViewModels.
* **Coroutines & StateFlow:** Smooth reactive state handling.
* **Multilingual Localizer:** Includes robust localization for English and Bengali to serve a broad audience.

## 🚀 Getting Started

To build and compile the application:

1. Clone or download the repository.
2. Run the Gradle build task to generate the APK:
   ```bash
   gradle assembleDebug
   ```
3. The APK will be available in the `app/build/outputs/apk/debug/` directory.

---
*Built with ❤️ for Solar Enthusiasts.*
