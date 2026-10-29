package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@SuppressLint("MissingPermission")
@Composable
fun BluetoothPrinterScreen(
    isArabic: Boolean,
    selectedDevice: BluetoothDevice?,
    onSelectDevice: (BluetoothDevice) -> Unit
) {
    val context = LocalContext.current
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter = bluetoothManager?.adapter

    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.all { it }
        if (hasPermission && bluetoothAdapter != null) {
            pairedDevices = bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsNeeded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        hasPermission = permissionsNeeded.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (hasPermission && bluetoothAdapter != null) {
            pairedDevices = bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
        } else {
            permissionLauncher.launch(permissionsNeeded)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isArabic) "إعدادات طابعة بلوتوث الحرارية" else "Bluetooth Thermal Printer Setup",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isArabic) "اختر طابعة البلوتوث الخاصة بك من القائمة أدناه المقترنة بالهاتف." else "Select your thermal Bluetooth printer from the paired devices list below.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (bluetoothAdapter != null) {
                            pairedDevices = bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isArabic) "تحديث قائمة الأجهزة المقترنة" else "Refresh Paired Devices")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isArabic) "الأجهزة المقترنة:" else "Paired Devices:",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (!hasPermission) {
            Text(
                text = if (isArabic) "يرجى منح أذونات البلوتوث للبحث عن الطابعات." else "Please grant Bluetooth permissions to discover printers.",
                color = MaterialTheme.colorScheme.error
            )
        } else if (pairedDevices.isEmpty()) {
            Text(
                text = if (isArabic) "لا توجد أجهزة بلوتوث مقترنة. يجدر إقتران الطابعة الحرارية من إعدادات البلوتوث بالهاتف أولاً." else "No paired Bluetooth devices found. Please pair your thermal printer in Android Bluetooth settings first.",
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pairedDevices) { device ->
                    val isSelected = selectedDevice?.address == device.address
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectDevice(device) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = device.name ?: "Unknown Thermal Printer",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = device.address,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
