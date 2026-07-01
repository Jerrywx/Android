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
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * 自定义圆环进度 —— ValueAnimator 驱动 invalidate() 在 onDraw 中重绘。
 */
class CircleProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 18f
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF1F2933")
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val rect = RectF()
    private var progress = 0f
    private var animator: ValueAnimator? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = ringPaint.strokeWidth
        rect.set(inset, inset, w - inset, h - inset)
        textPaint.textSize = w * 0.18f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        /// 背景圆环
        ringPaint.color = Color.parseColor("#FFE5E7EB")
        canvas.drawArc(rect, 0f, 360f, false, ringPaint)
        /// 前景进度圆环
        ringPaint.color = Color.parseColor("#FF07C160")
        canvas.drawArc(rect, -90f, 360f * progress, false, ringPaint)
        /// 中间百分比
        val percent = (progress * 100).toInt()
        val baseline = height / 2f - (textPaint.ascent() + textPaint.descent()) / 2f
        canvas.drawText("$percent%", width / 2f, baseline, textPaint)
    }

    fun animateTo(target: Float, duration: Long = 1500) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(progress, target).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
