package com.example.appdemo.demo.graphics.widget

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

/**
 * 双指缩放 ImageView —— 组合两个 GestureDetector：
 *
 *   · ScaleGestureDetector：双指缩放
 *   · GestureDetector：双击复位 + 单指拖动
 *
 * 通过 Matrix + setImageMatrix 应用变换，缩放范围钳制在 [1x, 5x]。
 */
class ZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AppCompatImageView(context, attrs) {

    private val matrix_ = Matrix()
    private var currentScale = 1f
    private val minScale = 1f
    private val maxScale = 5f

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val factor = detector.scaleFactor
            val target = (currentScale * factor).coerceIn(minScale, maxScale)
            val realFactor = target / currentScale
            currentScale = target
            matrix_.postScale(realFactor, realFactor, detector.focusX, detector.focusY)
            imageMatrix = matrix_
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            /// 双击：复位到 1x
            matrix_.reset()
            currentScale = 1f
            imageMatrix = matrix_
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            if (currentScale <= 1f) return false
            matrix_.postTranslate(-dx, -dy)
            imageMatrix = matrix_
            return true
        }
    })

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }
}
