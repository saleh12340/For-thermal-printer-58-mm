package com.example.ui.screens

import android.bluetooth.BluetoothDevice
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.utils.ReceiptBuilder
import com.example.utils.ThermalProcessor

@Composable
fun ReceiptTemplateScreen(
    isArabic: Boolean,
    selectedDevice: BluetoothDevice?,
    onPrintBitmap: (Bitmap) -> Unit
) {
    var storeName by remember { mutableStateOf(if (isArabic) "متجر القهوة السريعة" else "Express Coffee Shop") }
    var storeAddress by remember { mutableStateOf("شارع الملك فهد، الرياض") }
    var footerMessage by remember { mutableStateOf(if (isArabic) "شكراً لزيارتكم! نراكم قريباً" else "Thank you for your visit!") }
    var taxRate by remember { mutableDoubleStateOf(0.15) }

    val items = remember {
        mutableStateListOf(
            ReceiptBuilder.ReceiptItem(if (isArabic) "قهوة لاتيه" else "Café Latte", 2, 15.0),
            ReceiptBuilder.ReceiptItem(if (isArabic) "كرواسون زبدة" else "Butter Croissant", 1, 10.0),
            ReceiptBuilder.ReceiptItem(if (isArabic) "عصير برتقال" else "Orange Juice", 1, 12.0)
        )
    }

    var newItemName by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("1") }
    var newItemPrice by remember { mutableStateOf("10.0") }

    val receiptBitmap = remember(storeName, storeAddress, items.toList(), taxRate, footerMessage, isArabic) {
        ReceiptBuilder.createReceiptBitmap(
            storeName = storeName,
            storeAddress = storeAddress,
            items = items,
            taxRate = taxRate,
            footerMessage = footerMessage,
            isRtl = isArabic
        )
    }

    val processedBitmap = remember(receiptBitmap) {
        ThermalProcessor.prepareBitmap(receiptBitmap, ThermalProcessor.Adjustments(useDithering = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(if (isArabic) "تصميم الإيصال الحراري" else "Thermal Receipt Studio", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text(if (isArabic) "اسم المتجر" else "Store Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = storeAddress,
                    onValueChange = { storeAddress = it },
                    label = { Text(if (isArabic) "العنوان" else "Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(if (isArabic) "العناصر / المنتجات" else "Items", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))

                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${item.name} (x${item.qty}) - $%.2f".format(item.qty * item.price), modifier = Modifier.weight(1f))
                        IconButton(onClick = { items.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text(if (isArabic) "اسم العنصر" else "Item name") },
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = newItemQty,
                        onValueChange = { newItemQty = it },
                        label = { Text("الكمية") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = newItemPrice,
                        onValueChange = { newItemPrice = it },
                        label = { Text("السعر") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newItemName.isNotBlank()) {
                            val qty = newItemQty.toIntOrNull() ?: 1
                            val price = newItemPrice.toDoubleOrNull() ?: 10.0
                            items.add(ReceiptBuilder.ReceiptItem(newItemName, qty, price))
                            newItemName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isArabic) "إضافة عنصر للإيصال" else "Add Item")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(if (isArabic) "معاينة الإيصال الحراري" else "Live Receipt Preview", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .width(300.dp)
                .heightIn(max = 400.dp)
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.TopCenter) {
                Image(
                    bitmap = processedBitmap.asImageBitmap(),
                    contentDescription = "Receipt Preview",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onPrintBitmap(processedBitmap) },
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("print_receipt_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Print, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isArabic) "طباعة الإيصال عبر البلوتوث" else "PRINT RECEIPT OVER BLUETOOTH", style = MaterialTheme.typography.titleMedium)
        }
    }
}
