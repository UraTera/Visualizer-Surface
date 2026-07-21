package com.tera.visualizer.drawing

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.tera.visualizer.FftParams

class DrawWare(columns: Int) {

    private val paint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
    }

    private var targetHeights = FloatArray(columns) // Целевые значения высоты
    private var currentHeights = FloatArray(columns) // Текущие анимированные высоты столбцов
    private var smoothingFactor = 0.2f // Коэффициент сглаживания (чем меньше, тем плавнее и медленнее движение)

    // Плавное приближение текущей высоты к целевой (Lerp)
    private fun updateHeights() {
        for (i in currentHeights.indices) {
            currentHeights[i] += (targetHeights[i] - currentHeights[i]) * smoothingFactor
        }
    }

    fun drawing(canvas: Canvas, data: FloatArray){
        if (data.size != targetHeights.size) return

        targetHeights = data.clone()
        updateHeights()

        val barColor = FftParams.barColor
        val barCount = data.size

        val yOff = 20f
        val max = (canvas.height / 2 - yOff * 1.5f)
        val scale = max / 120f

        val blockWidth = (canvas.width / barCount).toFloat()
        val gap = blockWidth * 0.5f // Ширина пробела
        val barWidth = blockWidth - gap
        paint.strokeWidth = barWidth

        val y0 = canvas.height / 2
        val x0 = blockWidth / 2 + gap / 4

        for (i in 0 until barCount) {
            val x = x0 + i * blockWidth
            var amplitude = currentHeights[i] * scale
            amplitude = amplitude.coerceAtMost(max)

            val y1 = y0 + amplitude
            val y2 = y0 - amplitude

            if (FftParams.colorEnabled) {
                val interpolation = amplitude / y0
                paint.color = when {
                    interpolation > 0.8f -> Color.RED
                    interpolation > 0.5f -> Color.YELLOW
                    else -> barColor
                }
            } else paint.color = barColor

            if (amplitude > 0.1)
                canvas.drawLine(x, y1, x, y2, paint)
        }
    }
}