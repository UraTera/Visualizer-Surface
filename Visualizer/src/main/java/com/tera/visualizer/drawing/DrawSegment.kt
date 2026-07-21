package com.tera.visualizer.drawing

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.tera.visualizer.FftParams

class DrawSegment(columns: Int) {

    private val paint = Paint()

    init {
        paint.strokeCap = Paint.Cap.ROUND
    }

    private var targetHeights = FloatArray(columns)
    private var currentHeights = FloatArray(columns)
    private var smoothingFactor = 0.2f

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

        val max = (canvas.height - 40f)
        val scale = max / 120f

        val horizonStep = canvas.width.toFloat() / barCount
        val gap = horizonStep * 0.3f // Промежуток между столбцами
        val segmentHeight = FftParams.segmentHeight // Высота сегмента
        val x0 = segmentHeight / 2 + gap / 4
        val segmentWidth = horizonStep - segmentHeight - gap / 2
        paint.strokeWidth = segmentHeight

        val verticalGap = 4f    // Расстояние между сегментами по вертикали
        val segmentStep = segmentHeight + verticalGap // Шаг сетки по вертикали

        for (i in 0 until barCount) {

            val x1 = x0 + i * horizonStep
            val x2 = x1 + segmentWidth

            var amplitude = currentHeights[i] * scale
            amplitude = amplitude.coerceAtMost(max)

            var drawnHeight = 0f

            while (drawnHeight + segmentHeight < amplitude) {
                val y = canvas.height - drawnHeight - 28f

                // Вычисляем цвет конкретного сегмента в зависимости от его высоты
                if (FftParams.colorEnabled) {
                    val heightRatio = drawnHeight / canvas.height.toFloat()
                    paint.color = when {
                        heightRatio > 0.75f -> Color.RED
                        heightRatio > 0.5f -> Color.YELLOW
                        else -> barColor
                    }
                } else paint.color = barColor

                // Рисуем сегмент
                canvas.drawLine(x1, y, x2, y, paint)

                // Переходим к следующему сегменту выше
                drawnHeight += segmentStep
            }
        }
    }


}