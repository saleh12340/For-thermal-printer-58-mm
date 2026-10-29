package com.example.ui.screens

import android.bluetooth.BluetoothDevice
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.utils.ThermalProcessor
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun FileViewerScreen(
    isArabic: Boolean,
    selectedDevice: BluetoothDevice?,
    onPrintBitmap: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }

    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var threshold by remember { mutableIntStateOf(128) }
    var useDithering by remember { mutableStateOf(true) }
    var invert by remember { mutableStateOf(false) }
    var rotation by remember { mutableIntStateOf(0) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            fileName = it.lastPathSegment ?: "document"
            val mimeType = context.contentResolver.getType(it) ?: ""
            try {
                if (mimeType.contains("pdf") || fileName!!.endsWith(".pdf", true)) {
                    val pfd: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(it, "r")
                    if (pfd != null) {
                        val renderer = PdfRenderer(pfd)
                        val page = renderer.openPage(0)
                        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        page.close()
                        renderer.close()
                        pfd.close()
                        originalBitmap = bitmap
                    }
                } else if (mimeType.contains("image") || fileName!!.matches(Regex(".*\\.(png|jpg|jpeg|webp|bmp)$", RegexOption.IGNORE_CASE))) {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    originalBitmap = bitmap
                } else {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val textBuilder = StringBuilder()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        textBuilder.append(line).append("\n")
                        line = reader.readLine()
                    }
                    reader.close()
                    inputStream?.close()

                    val text = textBuilder.toString()
                    val paint = android.graphics.Paint().apply {
                        textSize = 24f
                        color = android.graphics.Color.BLACK
                        isAntiAlias = true
                    }
                    val width = ThermalProcessor.THERMAL_WIDTH_PX
                    val lines = text.split("\n")
                    val height = maxOf(200, lines.size * 35)
                    val textBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(textBitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)

                    var y = 30f
                    for (l in lines) {
                        canvas.drawText(l, 10f, y, paint)
                        y += 35f
                    }
                    originalBitmap = textBitmap
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val processedBitmap = remember(originalBitmap, brightness, contrast, threshold, useDithering, invert, rotation) {
        originalBitmap?.let {
            ThermalProcessor.prepareBitmap(
                it,
                ThermalProcessor.Adjustments(
                    brightness = brightness,
                    contrast = contrast,
                    threshold = threshold,
                    useDithering = useDithering,
                    invert = invert,
                    rotationDegrees = rotation
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isArabic) "اختر ملفاً أو صورة للطباعة الحرارية" else "Select File or Image for Thermal Printing",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { filePicker.launch(arrayOf("application/pdf", "image/*", "text/*", "*/*")) },
                    modifier = Modifier.fillMaxWidth().testTag("select_file_button")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isArabic) "اختر ملف (PDF, صورة, نص)" else "Choose File (PDF, Image, Text)")
                }
                if (fileName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "${if (isArabic) "الملف المحدد:" else "Selected:"} $fileName", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (processedBitmap != null) {
            Text(
                text = if (isArabic) "معاينة إيصال 58 مم (384 بكسل)" else "58mm Thermal Receipt Preview (384px)",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .width(300.dp)
                    .heightIn(max = 400.dp)
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(
                        bitmap = processedBitmap.asImageBitmap(),
                        contentDescription = "Thermal Receipt Preview",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (isArabic) "إعدادات المعالجة الحرارية" else "Thermal Processing Controls", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isArabic) "فلوريد-ستاينبرغ (Dithering)" else "Floyd-Steinberg Dithering")
                        Switch(checked = useDithering, onCheckedChange = { useDithering = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isArabic) "طباعة سالبة (Invert)" else "Negative Invert")
                        Switch(checked = invert, onCheckedChange = { invert = it })
                    }

                    Text("${if (isArabic) "السطوع" else "Brightness"}: ${brightness.toInt()}")
                    Slider(
                        value = brightness,
                        onValueChange = { brightness = it },
                        valueRange = -100f..100f
                    )

                    Text("${if (isArabic) "التباين" else "Contrast"}: ${"%.1f".format(contrast)}x")
                    Slider(
                        value = contrast,
                        onValueChange = { contrast = it },
                        valueRange = 0.5f..2.0f
                    )

                    if (!useDithering) {
                        Text("${if (isArabic) "العتبة (Threshold)" else "Threshold"}: $threshold")
                        Slider(
                            value = threshold.toFloat(),
                            onValueChange = { threshold = it.toInt() },
                            valueRange = 0f..255f
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(onClick = { rotation = (rotation + 90) % 360 }) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${rotation}°")
                        }
                        OutlinedButton(onClick = {
                            brightness = 0f
                            contrast = 1f
                            threshold = 128
                            rotation = 0
                            invert = false
                        }) {
                            Text(if (isArabic) "إعادة ضبط" else "Reset")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onPrintBitmap(processedBitmap) },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("print_now_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isArabic) "طباعة الآن عبر البلوتوث" else "PRINT NOW OVER BLUETOOTH", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Spacer(modifier = Modifier.height(40.dp))
            Icon(
                Icons.Default.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isArabic) "لم يتم اختيار ملف بعد. اضغط أعلاه لاختيار PDF أو صورة." else "No file selected yet. Tap above to select PDF, image, or text.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
