package com.example.appdemo.demo.customview.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * 贝塞尔曲线波浪。
 *
 * · 用 Path + 二阶贝塞尔 quadTo 画出一个完整周期的波形（波峰 + 波谷）
 * · 横向铺满多个周期，再多加一个周期，通过 translate 无缝滚动
 * · 前后两条波浪相位错开、颜色半透明叠加，形成层次感
 * · progress 决定水位（0..100），改变波浪的静止基线高度
 */
class WaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val waveHeight = dp(14f)
    private val waveLength = dp(160f)

    private val backPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#664FC3F7")
    }
    private val frontPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AA1E88E5")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = dp(28f)
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val backPath = Path()
    private val frontPath = Path()

    /** 波浪水平位移，0..waveLength 循环 */
    private var offset = 0f
    private var progress = 60

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1800
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            offset = (it.animatedValue as Float) * waveLength
            invalidate()
        }
    }

    fun setProgress(value: Int) {
        progress = value.coerceIn(0, 100)
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val baseline = h * (1f - progress / 100f)

        /// 后层波浪：向左偏移
        buildWavePath(backPath, w, h, baseline, -offset)
        canvas.drawPath(backPath, backPaint)

        /// 前层波浪：反向偏移 + 相位差半个波长，形成叠加错位
        buildWavePath(frontPath, w, h, baseline, offset - waveLength / 2f)
        canvas.drawPath(frontPath, frontPaint)

        val label = "$progress%"
        val fm = textPaint.fontMetrics
        val cy = h / 2f - (fm.ascent + fm.descent) / 2f
        canvas.drawText(label, w / 2f, cy, textPaint)
    }

    private fun buildWavePath(path: Path, w: Float, h: Float, baseline: Float, shift: Float) {
        path.reset()
        val startX = shift - waveLength
        path.moveTo(startX, baseline)
        var x = startX
        /// 每个周期由两段二阶贝塞尔组成：先向上一个波峰，再向下一个波谷
        while (x < w + waveLength) {
            path.quadTo(
                x + waveLength / 4f, baseline - waveHeight,
                x + waveLength / 2f, baseline,
            )
            path.quadTo(
                x + waveLength * 3f / 4f, baseline + waveHeight,
                x + waveLength, baseline,
            )
            x += waveLength
        }
        path.lineTo(x, h)
        path.lineTo(startX, h)
        path.close()
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
