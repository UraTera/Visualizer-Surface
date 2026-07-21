package com.tera.visualizer_surface

import android.graphics.Canvas
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.tera.visualizer_surface.drawing.DrawSegment
import com.tera.visualizer_surface.drawing.DrawSolid
import com.tera.visualizer_surface.drawing.DrawWare

class Engine(surfaceView: SurfaceView) {

    private var drawWare = DrawWare(FftParams.columns)
    private var drawSolid = DrawSolid(FftParams.columns)
    private var drawSegment = DrawSegment(FftParams.columns)
    private var renderThread: RenderThread? = null
    private var mData = FloatArray(0)
    private var isStart = false

    init {

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {

            override fun surfaceCreated(holder: SurfaceHolder) {
                renderThread = RenderThread(holder)
                renderThread!!.running = true
                renderThread!!.start()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                var retry = true
                renderThread?.running = false
                while (retry) {
                    try {
                        renderThread?.join()
                        retry = false
                    } catch (e: InterruptedException) {
                        // Ожидаем завершения потока отрисовки
                    }
                }
            }
        })
    }

    fun setData(data: FloatArray) {
        mData = data.clone()
    }

    fun setColumn(columns: Int) {
        drawWare = DrawWare(columns)
        drawSolid = DrawSolid(columns)
        drawSegment = DrawSegment(columns)
    }

    fun start(value: Boolean) {
        isStart = value
    }

    fun stop(value: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            isStart = value
        }, 1000)
    }

    // Бесконечный цикл отрисовки спектрограммы
    inner class RenderThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        var running = false

        override fun run() {

            while (running) {
                var canvas: Canvas? = null
                try {
                    canvas = surfaceHolder.lockCanvas()
                    if (canvas != null) {
                        synchronized(surfaceHolder) {
                            if (isStart) {
                                val groundColor = FftParams.groundColor
                                canvas.drawColor(groundColor) // Фон
                                val style = FftParams.style
                                when (style) {
                                    0 -> drawWare.drawing(canvas, mData)
                                    1 -> drawSolid.drawing(canvas, mData)
                                    2 -> drawSegment.drawing(canvas, mData)
                                    else -> drawWare.drawing(canvas, mData)
                                }
                            }
                        }
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
                try {
                    sleep(16)
                } catch (e: InterruptedException) {
                } // Ограничение ~60 FPS
            }
        }
    }

}