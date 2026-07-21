package com.tera.visualizer

import android.graphics.Color

object FftParams {
    var columns = 32
    var style = 0
    var groundColor = Color.BLACK
    var barColor = Color.GREEN
    var colorEnabled = false
    var segmentHeight = 12f

    fun styleVisual(value: Int) {
        style = value
    }

    fun groundColor(value: Int) {
        groundColor = value
    }

    fun barColor(value: Int) {
        barColor = value
    }

    fun colorEnabled(value: Boolean) {
        colorEnabled = value
    }

    fun segmentHeight(value: Int) {
        segmentHeight = value.toFloat()
    }

}