package com.example.appdemo.demo.animation.widget

import com.example.appdemo.R
import com.example.appdemo.common.*
import com.example.appdemo.tabs.*
import com.example.appdemo.demo.layout.*
import com.example.appdemo.demo.concurrent.*
import com.example.appdemo.demo.network.*
import com.example.appdemo.demo.animation.*
import com.example.appdemo.demo.animation.widget.*
import com.example.appdemo.demo.viewpager.*
import com.example.appdemo.demo.recyclerview.*
import com.example.appdemo.demo.recyclerview.chat.*
import com.example.appdemo.demo.fragment.*

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * 水波纹扩散自定义 View ——
 * 4 个圆相位错开，半径从 0 扩到最大，alpha 从 1 衰减到 0。
 */
class RippleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var phase = 0f
    private val rippleCount = 4
    private var animator: ValueAnimator? = null

    init {
        post { start() }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val maxR = (minOf(width, height) / 2f) * 0.9f

        for (i in 0 until rippleCount) {
            /// 错开相位
            var t = phase + i.toFloat() / rippleCount
            t -= t.toInt()
            val r = maxR * t
            val alpha = ((1f - t) * 200).toInt().coerceIn(0, 255)
            paint.color = Color.argb(alpha, 7, 193, 96)
            canvas.drawCircle(cx, cy, r, paint)
        }

        /// 中心实心圆
        paint.color = Color.parseColor("#FF07C160")
        canvas.drawCircle(cx, cy, 24f, paint)
    }

    fun start() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                phase = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stop() {
        animator?.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
