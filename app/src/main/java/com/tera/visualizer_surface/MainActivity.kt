package com.tera.visualizer_surface

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tera.visualizer_surface.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    companion object {
        const val STYLE = "style"
        const val COLOR_ENABLED = "color_enabled"
    }

    private lateinit var binding: ActivityMainBinding

    private var visualizer: VisualizerManager? = null
    private var player: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private var maxVolume = 0
    private var currVolume = 0
    private var posVolume = 0f
    private var style = 0
    private var colorEnabled = false
    private var isPlaying = false

    private lateinit var sp: SharedPreferences

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            initVisualizer()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Отключить блокировку экрана
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        sp = getSharedPreferences("settings", MODE_PRIVATE)
        style = sp.getInt(STYLE, 0)
        colorEnabled = sp.getBoolean(COLOR_ENABLED, true)

        player = MediaPlayer.create(this, R.raw.demo)
        visualizer = VisualizerManager(this)
        visualizer!!.setView(binding.surface)
        visualizer!!.columns = 20
        visualizer!!.segmentHeight = 20

        setVolume()
        initButton()
        setParams()

        // Проверить разрешение
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            initVisualizer()
        else
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

    }

    private fun initVisualizer(){
        visualizer!!.init(player!!.audioSessionId)
    }

    private fun initButton() = with(binding) {

        imPlay.setOnClickListener {
            if (isPlaying) {
                imPlay.setImageResource(R.drawable.ic_play)
                player!!.stop()
                player!!.prepare()
                visualizer!!.stop()
                isPlaying = false
            } else {
                imPlay.setImageResource(R.drawable.ic_pause)
                player!!.start()
                visualizer!!.start()
                isPlaying = true
            }
        }

        chColor.setOnCheckedChangeListener { _, isChecked ->
            visualizer!!.colorEnabled = isChecked
            colorEnabled = isChecked
        }

        rgStyle.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.rbWave -> {
                    visualizer!!.style = 0
                    style = 0
                }

                R.id.rbBar -> {
                    visualizer!!.style = 1
                    style = 1
                }

                R.id.rbSeg -> {
                    visualizer!!.style = 2
                    style = 2
                }
            }
        }
    }

    // Громкость
    private fun setVolume() = with(binding) {
        // Получить аудио менеджер
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        // Установите максимальную громкость
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        // Установите текущую громкость
        currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        slVolume.valueMax = maxVolume.toFloat()
        slVolume.value = currVolume.toFloat()
        posVolume = slVolume.value

        // Громкость
        slVolume.setOnChangeListener {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it.toInt(), 0)
            posVolume = it
            visualizer!!.volume = it
            setParams()
        }
    }

    private fun setParams() = with(binding) {
        visualizer!!.style = style
        visualizer!!.groundColor = Color.BLACK
        visualizer!!.barColor = Color.GREEN

        visualizer!!.colorEnabled = colorEnabled
        chColor.isChecked = colorEnabled

        when (style) {
            0 -> rbWave.isChecked = true
            1 -> rbBar.isChecked = true
            2 -> rbSeg.isChecked = true
        }

        val percent = posVolume * 100 / maxVolume
        val volume = percent.toInt().toString() + " %"
        tvVolume.text = volume
    }

    override fun onStop() {
        super.onStop()
        sp.edit {
            putInt(STYLE, style)
            putBoolean(COLOR_ENABLED, colorEnabled)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        visualizer?.release()
    }
}