# MiniPrint Pro (طابعة الجيب الذكية) 🖨️📱

MiniPrint Pro is a production-ready, feature-rich Android application built with **Kotlin** and **Jetpack Compose** designed for 58mm Bluetooth thermal receipt printers.

---

## 🌟 Features / الميزات الرئيسية

- **Bilingual Interface (Arabic & English):** Full support for Arabic (RTL) and English (LTR) with dynamic layout switching.
- **Multi-Format File Ingestion:** Import and parse PDFs (`PdfRenderer`), images (`.png`, `.jpg`, `.webp`), and text/CSV files locally.
- **Thermal Print Pre-Processing Engine:**
  - 58mm paper width constraint enforcement (**384 pixels width at 203 DPI**).
  - Floyd-Steinberg dithering and binarization thresholding for clear 1-bit monochrome thermal prints.
  - Real-time brightness, contrast, rotation, and negative inversion controls.
- **Interactive Simulated Paper Roll Viewport:** Live visual preview of continuous receipt rolls.
- **Bluetooth ESC/POS Print Executor:**
  - Discover paired Bluetooth thermal printers, connect over SPP socket, and stream raw ESC/POS raster bitmap commands (`GS v 0`), line feeds, and auto-cut commands.
- **Receipt Template Studio & QR Tools:**
  - Custom receipt builder with items, taxes, and totals, plus QR code and checklist printers.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose & Material Design 3 (M3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Printing Protocols:** Bluetooth Classic RFCOMM (SPP) / ESC/POS command set
- **Document Rendering:** Android native `PdfRenderer` and `Bitmap` manipulation

---

## 🚀 Getting Started

1. Open the project in **Android Studio** (Koala or newer).
2. Sync project with Gradle files.
3. Connect your Android device or emulator, pair your 58mm Bluetooth thermal printer in Android Bluetooth settings.
4. Run the app and start printing!
