package com.example.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

object ReceiptBuilder {

    data class ReceiptItem(val name: String, val qty: Int, val price: Double)

    fun createReceiptBitmap(
        storeName: String,
        storeAddress: String,
        items: List<ReceiptItem>,
        taxRate: Double,
        footerMessage: String,
        isRtl: Boolean
    ): Bitmap {
        val width = ThermalProcessor.THERMAL_WIDTH_PX
        val padding = 16
        val contentWidth = width - (padding * 2)

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.MONOSPACE
        }

        val boldPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        }

        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 32f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        // Estimate height
        val estimatedHeight = 300 + (items.size * 40) + 200
        val bitmap = Bitmap.createBitmap(width, estimatedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        var y = 40f

        // Store Header
        canvas.drawText(storeName, width / 2f, y, titlePaint)
        y += 35f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(storeAddress, width / 2f, y, paint)
        y += 25f
        canvas.drawText("------------------------------------------------", width / 2f, y, paint)
        y += 30f

        paint.textAlign = Paint.Align.LEFT
        val leftX = padding.toFloat()
        val rightX = (width - padding).toFloat()

        // Items Header
        canvas.drawText("Item / الصنف", leftX, y, boldPaint)
        canvas.drawText("Total", rightX - 80f, y, boldPaint)
        y += 25f
        canvas.drawText("------------------------------------------------", leftX, y, paint)
        y += 30f

        var subtotal = 0.0
        for (item in items) {
            val itemTotal = item.qty * item.price
            subtotal += itemTotal
            canvas.drawText("${item.name} (x${item.qty})", leftX, y, paint)
            y += 25f
            canvas.drawText("$%.2f".format(itemTotal), rightX - 70f, y, paint)
            y += 30f
        }

        canvas.drawText("------------------------------------------------", leftX, y, paint)
        y += 30f

        val tax = subtotal * taxRate
        val total = subtotal + tax

        canvas.drawText("Subtotal / المجموع الفرعي:", leftX, y, paint)
        canvas.drawText("$%.2f".format(subtotal), rightX - 70f, y, paint)
        y += 30f

        canvas.drawText("Tax / الضريبة (${(taxRate * 100).toInt()}%):", leftX, y, paint)
        canvas.drawText("$%.2f".format(tax), rightX - 70f, y, paint)
        y += 35f

        canvas.drawText("TOTAL / الإجمالي:", leftX, y, boldPaint)
        canvas.drawText("$%.2f".format(total), rightX - 90f, y, boldPaint)
        y += 40f

        canvas.drawText("------------------------------------------------", leftX, y, paint)
        y += 40f

        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(footerMessage, width / 2f, y, paint)
        y += 40f
        canvas.drawText("*** MiniPrint Pro - طابعة الجيب الذكية ***", width / 2f, y, paint)

        // Crop to actual content height
        val finalHeight = (y + 40).toInt().coerceAtLeast(200)
        return Bitmap.createBitmap(bitmap, 0, 0, width, finalHeight)
    }
}
