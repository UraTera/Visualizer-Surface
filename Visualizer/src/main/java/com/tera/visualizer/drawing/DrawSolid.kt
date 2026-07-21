package com.tera.visualizer.drawing

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.tera.visualizer.FftParams

class DrawSolid(columns: Int) {

    private val paint = Paint()

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
        val max = (canvas.height - yOff * 2)
        val scale = max / 130f

        val blockWidth = (canvas.width / barCount).toFloat()
        val gap = blockWidth * 0.3f // Ширина пробела
        val barWidth = blockWidth - gap
        paint.strokeWidth = barWidth

        val x0 = blockWidth / 2 + gap / 2
        val y1 = canvas.height - yOff

        for (i in 0 until barCount) {
            val x = x0 + i * blockWidth
            var amplitude = currentHeights[i] * scale
            amplitude = amplitude.coerceAtMost(max)
            val y2 = y1 - amplitude

            if (FftParams.colorEnabled) {
                val interpolation = amplitude / canvas.height.toFloat()
                paint.color = when {
                    interpolation > 0.8f -> Color.RED
                    interpolation > 0.5f -> Color.YELLOW
                    else -> barColor
                }
            } else paint.color = barColor

            canvas.drawLine(x, y1, x, y2, paint)
        }
    }
}