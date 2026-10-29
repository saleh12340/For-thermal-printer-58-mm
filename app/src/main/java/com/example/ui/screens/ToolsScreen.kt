package com.example.ui.screens

import android.bluetooth.BluetoothDevice
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.utils.ThermalProcessor

@Composable
fun ToolsScreen(
    isArabic: Boolean,
    selectedDevice: BluetoothDevice?,
    onPrintBitmap: (Bitmap) -> Unit
) {
    var toolMode by remember { mutableIntStateOf(0) } // 0: Note, 1: QR Code, 2: Checklist
    var noteText by remember { mutableStateOf(if (isArabic) "ملاحظة سريعة من طابعة الجيب" else "Quick note from MiniPrint Pro") }
    var qrText by remember { mutableStateOf("https://github.com") }

    val generatedBitmap = remember(toolMode, noteText, qrText, isArabic) {
        val width = ThermalProcessor.THERMAL_WIDTH_PX
        val height = 400
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 28f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }

        var y = 40f
        if (toolMode == 0) {
            canvas.drawText(if (isArabic) "ملاحظة جيب / Memo" else "Pocket Memo", 20f, y, titlePaint)
            y += 40f
            canvas.drawText("------------------------------------------------", 10f, y, paint)
            y += 40f
            canvas.drawText(noteText, 20f, y, paint)
        } else if (toolMode == 1) {
            canvas.drawText(if (isArabic) "رمز الاستجابة السريعة (QR)" else "QR Code Label", 20f, y, titlePaint)
            y += 40f
            canvas.drawText("------------------------------------------------", 10f, y, paint)
            y += 40f
            val qrSize = 180
            val left = (width - qrSize) / 2
            val rectPaint = Paint().apply { color = Color.BLACK }
            canvas.drawRect(left.toFloat(), y, (left + qrSize).toFloat(), y + qrSize, rectPaint)
            val inner = 140
            val innerLeft = (width - inner) / 2
            val whitePaint = Paint().apply { color = Color.WHITE }
            canvas.drawRect(innerLeft.toFloat(), y + 20, (innerLeft + inner).toFloat(), y + qrSize - 20, whitePaint)
            
            paint.textSize = 18f
            canvas.drawText("QR: $qrText", 20f, y + qrSize + 30, paint)
        } else {
            canvas.drawText(if (isArabic) "قائمة المهام / To-Do" else "Checklist", 20f, y, titlePaint)
            y += 40f
            canvas.drawText("------------------------------------------------", 10f, y, paint)
            y += 40f
            val tasks = listOf(
                "[ ] " + (if (isArabic) "شراء القهوة" else "Buy coffee"),
                "[x] " + (if (isArabic) "فحص الطابعة" else "Check printer"),
                "[ ] " + (if (isArabic) "شحن البطارية" else "Charge battery")
            )
            for (t in tasks) {
                canvas.drawText(t, 20f, y, paint)
                y += 40f
            }
        }

        Bitmap.createBitmap(bmp, 0, 0, width, (y + 40).toInt().coerceAtMost(600))
    }

    val processedBitmap = remember(generatedBitmap) {
        ThermalProcessor.prepareBitmap(generatedBitmap, ThermalProcessor.Adjustments(useDithering = true))
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
                Text(if (isArabic) "أدوات وقوالب سريعة" else "Quick Tools & Templates", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = toolMode == 0,
                        onClick = { toolMode = 0 },
                        label = { Text(if (isArabic) "ملاحظة" else "Note") }
                    )
                    FilterChip(
                        selected = toolMode == 1,
                        onClick = { toolMode = 1 },
                        label = { Text("QR Code") }
                    )
                    FilterChip(
                        selected = toolMode == 2,
                        onClick = { toolMode = 2 },
                        label = { Text(if (isArabic) "مهام" else "Checklist") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (toolMode == 0) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text(if (isArabic) "نص الملاحظة" else "Note Text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (toolMode == 1) {
                    OutlinedTextField(
                        value = qrText,
                        onValueChange = { qrText = it },
                        label = { Text("QR Link / Text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .width(300.dp)
                .heightIn(max = 400.dp)
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
            color = androidx.compose.ui.graphics.Color.White,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.TopCenter) {
                Image(
                    bitmap = processedBitmap.asImageBitmap(),
                    contentDescription = "Tool Preview",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onPrintBitmap(processedBitmap) },
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("print_tool_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Print, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isArabic) "طباعة الأداة عبر البلوتوث" else "PRINT TOOL OVER BLUETOOTH", style = MaterialTheme.typography.titleMedium)
        }
    }
}
