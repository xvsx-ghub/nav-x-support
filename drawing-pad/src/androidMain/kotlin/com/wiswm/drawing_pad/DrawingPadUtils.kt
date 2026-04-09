package com.wiswm.drawing_pad

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath

lateinit var drawingPadContext: Context

actual class DrawingPadUtils {
    companion object {
        private const val TAG = "DrawingPadUtils"
    }

    actual fun saveStrokesToPng(
        strokeList: List<DrawingPadViewModel.Stroke>,
        width: Int,
        height: Int
    ): String? {
        val bitmap = createBitmap(width, height)
        val canvas = AndroidCanvas(bitmap)

        canvas.drawColor(Color.WHITE)

        val paint = AndroidPaint().apply {
            isAntiAlias = true
            strokeCap = AndroidPaint.Cap.ROUND
            strokeJoin = AndroidPaint.Join.ROUND
            style = AndroidPaint.Style.STROKE
            color = Color.BLACK
        }

        for (s in strokeList) {
            if (s.points.size < 2) continue
            val path = AndroidPath()
            val scaleX = width.toFloat() / (width)
            val scaleY = height.toFloat() / (height)

            val first = s.points.first()
            path.moveTo(first.x * scaleX, first.y * scaleY)
            for (pt in s.points.drop(1)) {
                path.lineTo(pt.x * scaleX, pt.y * scaleY)
            }
            paint.strokeWidth = s.strokeWidth
            canvas.drawPath(path, paint)
        }

        val filePath = createFilePath(drawingPadContext) ?: return null
        try {
            FileOutputStream(filePath).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Log.d(TAG, "File saved successfully: $filePath")
            return filePath
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save bitmap: ${e.message}")
        }
        return null
    }

    private fun createFilePath(context: Context): String? {
        return try {
            val dir = File(context.filesDir, "signatures")
            if (!dir.exists()) dir.mkdirs()
            val filename = "image_${System().getCurrentTimeMillis()}.jpg"
            File(dir, filename).absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create file path: ${e.message}")
            null
        }
    }

    actual fun deleteFolder(): Boolean {
        return try {
            val dir = File(drawingPadContext.filesDir, "signatures")
            if (dir.exists()) {
                dir.deleteRecursively()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete folder: ${e.message}")
            false
        }
    }
}