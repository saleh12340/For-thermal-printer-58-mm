package com.example.utils

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.graphics.Bitmap
import android.graphics.Color
import java.io.OutputStream
import java.util.UUID

object EscPosPrinter {
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val INIT = byteArrayOf(0x1B, 0x40)
    private val CUT_PAPER = byteArrayOf(0x1D, 0x56, 0x41, 0x10)

    fun printBitmapOverBluetooth(device: BluetoothDevice, bitmap: Bitmap): Result<Unit> {
        var socket: BluetoothSocket? = null
        return try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()
            val outputStream: OutputStream = socket.outputStream

            outputStream.write(INIT)
            val rasterBytes = bitmapToRasterBytes(bitmap)
            outputStream.write(rasterBytes)

            outputStream.write(byteArrayOf(0x1B, 0x64, 0x04)) // feed 4 lines
            outputStream.write(CUT_PAPER)
            outputStream.flush()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    private fun bitmapToRasterBytes(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val stride = (width + 7) / 8
        val data = ByteArray(8 + stride * height)

        data[0] = 0x1D
        data[1] = 0x76
        data[2] = 0x30
        data[3] = 0x00
        data[4] = (stride and 0xFF).toByte()
        data[5] = ((stride shr 8) and 0xFF).toByte()
        data[6] = (height and 0xFF).toByte()
        data[7] = ((height shr 8) and 0xFF).toByte()

        var k = 8
        for (y in 0 until height) {
            for (xStep in 0 until stride) {
                var slice = 0
                for (bit in 0..7) {
                    val x = xStep * 8 + bit
                    if (x < width) {
                        val pixel = bitmap.getPixel(x, y)
                        val isBlack = Color.red(pixel) < 128
                        if (isBlack) {
                            slice = slice or (1 shl (7 - bit))
                        }
                    }
                }
                data[k++] = slice.toByte()
            }
        }
        return data
    }
}
