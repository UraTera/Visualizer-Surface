package com.tera.visualizer

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.audiofx.Visualizer
import android.media.audiofx.Visualizer.OnDataCaptureListener
import android.view.SurfaceView
import kotlin.math.hypot

class VisualizerManager(context: Context) {

    private var visualizer: Visualizer? = null
    private lateinit var engine: Engine
    private var mColumn = FftParams.columns
    private var mAudioManager: AudioManager =
        context.getSystemService(AUDIO_SERVICE) as AudioManager
    private var mMaxVolume = 0
    private var mCurrVolume = 0
    private var mVolume = 0f // 0.0 - 1.0

    init {
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        mCurrVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mVolume = mCurrVolume.toFloat() / mMaxVolume
    }

    fun setView(view: SurfaceView){
        engine = Engine(view)
    }

    fun init(audioSession: Int) {
        visualizer = Visualizer(audioSession)
        visualizer!!.enabled = false
        visualizer!!.captureSize = 512

        // Регистрируем слушатель и указываем, какие данные нам нужны
        visualizer!!.setDataCaptureListener(object : OnDataCaptureListener {

            // Обрабатываем частотный спектр
            override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                processFFT(fft!!)
                //Log.d("myLogs", "fft: ${fft.toList()}")
            }

            // Обрабатываем волновую форму
            override fun onWaveFormDataCapture(
                visualizer: Visualizer?,
                waveform: ByteArray?,
                samplingRate: Int
            ) {
                // Ничего не делать
            }
        }, Visualizer.getMaxCaptureRate(), false, true)
    }

    private fun processFFT(fft: ByteArray) {
        val size = mColumn
        val data = FloatArray(size)

        for (i in 0 until size) {
            val r = fft[2 * i].toFloat()
            val j = fft[2 * i + 1].toFloat()
            data[i] = hypot(r, j)  * mVolume
        }
        engine.setData(data)
    }

    fun start() {
        visualizer!!.enabled = true
        engine.start(true)

    }

    fun stop() {
        visualizer!!.enabled = false
        engine.stop(false)
    }

    fun release() {
        visualizer?.enabled = false
        visualizer?.release()
    }

    // Свойства
    var volume: Float = 1f // 0.0 - 1.0
        set(value) {
            mVolume = value / mMaxVolume
        }

    var style: Int = 0
        set(value) {
            FftParams.styleVisual(value)
        }

    var barColor: Int = 0
        set(value) {
            FftParams.barColor(value)
        }

    var groundColor: Int = 0
        set(value) {
            FftParams.groundColor(value)
        }

    var colorEnabled: Boolean = false
        set(value) {
            FftParams.colorEnabled(value)
        }

    var columns: Int = mColumn
        set(value) {
            mColumn = value
            engine.setColumn(value)
        }

    var segmentHeight: Int = 15
        set(value) {
            FftParams.segmentHeight(value)
        }

}