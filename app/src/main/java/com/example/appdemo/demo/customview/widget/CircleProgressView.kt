package com.example.appdemo.demo.customview.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.min

/**
 * 圆形进度条。
 *
 * · 背景轨道：drawArc + Paint.STROKE
 * · 进度弧：drawArc + SweepGradient 渐变 + ROUND 端点
 * · 中心文字：drawText，居中通过 FontMetrics 修正
 * · 平滑过渡：ValueAnimator 从当前值动画到目标值
 */
class CircleProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val strokeWidthPx = dp(12f)

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        color = Color.parseColor("#22000000")
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#222222")
        textAlign = Paint.Align.CENTER
        textSize = dp(28f)
        isFakeBoldText = true
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#888888")
        textAlign = Paint.Align.CENTER
        textSize = dp(12f)
    }

    private val arcRect = RectF()
    private var progress = 0

    private var animator: ValueAnimator? = null

    fun animateTo(target: Int) {
        val clamped = target.coerceIn(0, 100)
        animator?.cancel()
        animator = ValueAnimator.ofInt(progress, clamped).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Int
                invalidate()
            }
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = min(w, h).toFloat()
        val half = strokeWidthPx / 2f + dp(4f)
        val left = (w - size) / 2f + half
        val top = (h - size) / 2f + half
        arcRect.set(left, top, left + size - half * 2, top + size - half * 2)

        /// SweepGradient 从 12 点方向开始（旋转 -90°），配色由浅到深
        val cx = arcRect.centerX()
        val cy = arcRect.centerY()
        progressPaint.shader = SweepGradient(
            cx, cy,
            intArrayOf(
                Color.parseColor("#4FC3F7"),
                Color.parseColor("#1E88E5"),
                Color.parseColor("#4FC3F7"),
            ),
            null,
        )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawArc(arcRect, 0f, 360f, false, trackPaint)

        val sweep = progress / 100f * 360f
        /// 从 12 点开始（-90° 起始角）
        canvas.drawArc(arcRect, -90f, sweep, false, progressPaint)

        val cx = arcRect.centerX()
        val cy = arcRect.centerY()
        val fm = textPaint.fontMetrics
        val baseline = cy - (fm.ascent + fm.descent) / 2f
        canvas.drawText("$progress%", cx, baseline, textPaint)
        canvas.drawText("已完成", cx, baseline + dp(22f), hintPaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
