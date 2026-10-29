package com.example.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import kotlin.math.max
import kotlin.math.min

object ThermalProcessor {
    const val THERMAL_WIDTH_PX = 384 // Standard 58mm thermal paper printable width at 203 DPI

    data class Adjustments(
        val brightness: Float = 0f, // -100 to 100
        val contrast: Float = 1f,   // 0.5 to 2.0
        val threshold: Int = 128,   // 0 to 255
        val useDithering: Boolean = true,
        val invert: Boolean = false,
        val rotationDegrees: Int = 0
    )

    fun prepareBitmap(source: Bitmap, adj: Adjustments): Bitmap {
        // 1. Rotate if needed
        var processed = if (adj.rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(adj.rotationDegrees.toFloat()) }
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        } else {
            source
        }

        // 2. Scale to 384px width preserving aspect ratio
        val targetWidth = THERMAL_WIDTH_PX
        val targetHeight = max(1, (targetWidth.toFloat() / processed.width * processed.height).toInt())
        val scaled = Bitmap.createScaledBitmap(processed, targetWidth, targetHeight, true)

        // 3. Convert to Monochrome Binarized Bitmap with Dithering / Threshold
        val width = scaled.width
        val height = scaled.height
        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)

        // First pass: grayscale with brightness & contrast
        val grayPixels = FloatArray(width * height)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = Color.red(p)
            val g = Color.green(p)
            val b = Color.blue(p)
            
            // Luminance
            var gray = 0.299f * r + 0.587f * g + 0.114f * b
            
            // Apply brightness
            gray += adj.brightness * 1.2f
            
            // Apply contrast
            gray = ((gray / 255f - 0.5f) * adj.contrast + 0.5f) * 255f
            
            grayPixels[i] = gray.coerceIn(0f, 255f)
        }

        val outPixels = IntArray(width * height)

        if (adj.useDithering) {
            // Floyd-Steinberg Dithering
            val buffer = grayPixels.clone()
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val idx = y * width + x
                    val oldPixel = buffer[idx]
                    val newPixel = if (oldPixel >= adj.threshold) 255f else 0f
                    val error = oldPixel - newPixel
                    
                    var finalVal = if (newPixel > 128f) 255 else 0
                    if (adj.invert) finalVal = 255 - finalVal
                    outPixels[idx] = if (finalVal == 255) Color.WHITE else Color.BLACK

                    // Distribute error
                    if (x + 1 < width) {
                        buffer[idx + 1] += error * 7f / 16f
                    }
                    if (y + 1 < height) {
                        if (x > 0) {
                            buffer[(y + 1) * width + x - 1] += error * 3f / 16f
                        }
                        buffer[(y + 1) * width + x] += error * 5f / 16f
                        if (x + 1 < width) {
                            buffer[(y + 1) * width + x + 1] += error * 1f / 16f
                        }
                    }
                }
            }
        } else {
            // Simple threshold
            for (i in grayPixels.indices) {
                var isWhite = grayPixels[i] >= adj.threshold
                if (adj.invert) isWhite = !isWhite
                outPixels[i] = if (isWhite) Color.WHITE else Color.BLACK
            }
        }

        val finalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        finalBitmap.setPixels(outPixels, 0, width, 0, 0, width, height)
        return finalBitmap
    }
}
