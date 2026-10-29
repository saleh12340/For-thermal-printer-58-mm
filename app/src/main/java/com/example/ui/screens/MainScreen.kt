package com.example.ui.screens

import android.bluetooth.BluetoothDevice
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.utils.EscPosPrinter
import com.example.utils.ThermalProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isArabic: Boolean,
    onToggleLanguage: () -> Unit,
    selectedDevice: BluetoothDevice?,
    onSelectDevice: (BluetoothDevice?) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val titlesAr = listOf("ملف/صورة", "إيصال", "الطابعة", "أدوات QR")
    val titlesEn = listOf("File/Image", "Receipt", "Printer", "QR Tools")
    val currentTitles = if (isArabic) titlesAr else titlesEn

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "MiniPrint Pro - طابعة الجيب" else "MiniPrint Pro")
                    }
                },
                actions = {
                    // Language Switcher Button
                    TextButton(onClick = onToggleLanguage) {
                        Text(if (isArabic) "EN" else "عربي", style = MaterialTheme.typography.bodyLarge)
                    }
                    // Bluetooth Status Indicator
                    IconButton(onClick = { selectedTab = 2 }) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = "Bluetooth Status",
                            tint = if (selectedDevice != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        bottomBar = {
            NavigationBar {
                currentTitles.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.Description, contentDescription = null)
                                1 -> Icon(Icons.Default.Receipt, contentDescription = null)
                                2 -> Icon(Icons.Default.Print, contentDescription = null)
                                else -> Icon(Icons.Default.QrCode, contentDescription = null)
                            }
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (statusMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(statusMessage!!, style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = { statusMessage = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss")
                            }
                        }
                    }
                }

                when (selectedTab) {
                    0 -> FileViewerScreen(
                        isArabic = isArabic,
                        selectedDevice = selectedDevice,
                        onPrintBitmap = { bitmap ->
                            if (selectedDevice == null) {
                                statusMessage = if (isArabic) "الرجاء اختيار طابعة بلوتوث أولاً" else "Please select a Bluetooth printer first"
                            } else {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val result = EscPosPrinter.printBitmapOverBluetooth(selectedDevice, bitmap)
                                    statusMessage = if (result.isSuccess) {
                                        if (isArabic) "تمت الطباعة بنجاح!" else "Printed successfully!"
                                    } else {
                                        if (isArabic) "فشلت الطباعة: ${result.exceptionOrNull()?.message}" else "Print failed: ${result.exceptionOrNull()?.message}"
                                    }
                                }
                            }
                        }
                    )
                    1 -> ReceiptTemplateScreen(
                        isArabic = isArabic,
                        selectedDevice = selectedDevice,
                        onPrintBitmap = { bitmap ->
                            if (selectedDevice == null) {
                                statusMessage = if (isArabic) "الرجاء اختيار طابعة بلوتوث أولاً" else "Please select a Bluetooth printer first"
                            } else {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val result = EscPosPrinter.printBitmapOverBluetooth(selectedDevice, bitmap)
                                    statusMessage = if (result.isSuccess) {
                                        if (isArabic) "تمت طباعة الإيصال بنجاح!" else "Receipt printed successfully!"
                                    } else {
                                        if (isArabic) "فشلت الطباعة: ${result.exceptionOrNull()?.message}" else "Print failed: ${result.exceptionOrNull()?.message}"
                                    }
                                }
                            }
                        }
                    )
                    2 -> BluetoothPrinterScreen(
                        isArabic = isArabic,
                        selectedDevice = selectedDevice,
                        onSelectDevice = { device ->
                            onSelectDevice(device)
                            statusMessage = if (isArabic) "تم الاتصال بـ ${device.name}" else "Connected to ${device.name}"
                        }
                    )
                    3 -> ToolsScreen(
                        isArabic = isArabic,
                        selectedDevice = selectedDevice,
                        onPrintBitmap = { bitmap ->
                            if (selectedDevice == null) {
                                statusMessage = if (isArabic) "الرجاء اختيار طابعة بلوتوث أولاً" else "Please select a Bluetooth printer first"
                            } else {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val result = EscPosPrinter.printBitmapOverBluetooth(selectedDevice, bitmap)
                                    statusMessage = if (result.isSuccess) {
                                        if (isArabic) "تمت الطباعة بنجاح!" else "Printed successfully!"
                                    } else {
                                        if (isArabic) "فشلت الطباعة: ${result.exceptionOrNull()?.message}" else "Print failed: ${result.exceptionOrNull()?.message}"
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
