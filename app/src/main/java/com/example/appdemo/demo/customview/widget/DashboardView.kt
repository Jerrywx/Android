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
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 仪表盘。
 *
 * · 270° 弧形轨道 + 渐变进度弧（起始角 135°，扫过角 270°）
 * · 沿弧一圈刻度：主刻度 10 段，短刻度间隔 3 段
 * · 中心指针：save/rotate 后画一条从中心向外的线，末端加一个圆头
 * · ValueAnimator 平滑到目标值
 */
class DashboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val startAngle = 135f
    private val sweepAngle = 270f
    private val maxValue = 100
    private val majorTicks = 10
    private val minorPerMajor = 4

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(10f)
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#22000000")
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(10f)
        strokeCap = Paint.Cap.ROUND
    }
    private val majorTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = dp(2f)
        color = Color.parseColor("#333333")
    }
    private val minorTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = dp(1f)
        color = Color.parseColor("#88666666")
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        textSize = dp(11f)
        textAlign = Paint.Align.CENTER
    }
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(4f)
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#E53935")
    }
    private val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#222222")
        textAlign = Paint.Align.CENTER
        textSize = dp(32f)
        isFakeBoldText = true
    }
    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#888888")
        textAlign = Paint.Align.CENTER
        textSize = dp(12f)
    }

    private val arcRect = RectF()
    private var cx = 0f
    private var cy = 0f
    private var radius = 0f

    private var value = 0f
    private var animator: ValueAnimator? = null

    fun animateTo(target: Int) {
        val clamped = target.coerceIn(0, maxValue).toFloat()
        animator?.cancel()
        animator = ValueAnimator.ofFloat(value, clamped).apply {
            duration = 900
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                value = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = min(w, h).toFloat()
        val pad = dp(20f)
        val left = (w - size) / 2f + pad
        val top = (h - size) / 2f + pad
        arcRect.set(left, top, left + size - pad * 2, top + size - pad * 2)
        cx = arcRect.centerX()
        cy = arcRect.centerY()
        radius = arcRect.width() / 2f

        progressPaint.shader = SweepGradient(
            cx, cy,
            intArrayOf(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#F44336"),
            ),
            floatArrayOf(0f, 0.6f, 0.85f),
        )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawArc(arcRect, startAngle, sweepAngle, false, trackPaint)

        val sweep = value / maxValue * sweepAngle
        canvas.drawArc(arcRect, startAngle, sweep, false, progressPaint)

        drawTicks(canvas)
        drawPointer(canvas)
        drawText(canvas)
    }

    private fun drawTicks(canvas: Canvas) {
        val outer = radius - dp(14f)
        val majorInner = outer - dp(10f)
        val minorInner = outer - dp(5f)
        val labelR = outer - dp(24f)
        val totalTicks = majorTicks * (minorPerMajor + 1)
        for (i in 0..totalTicks) {
            val angleDeg = startAngle + sweepAngle * i / totalTicks
            val rad = Math.toRadians(angleDeg.toDouble())
            val cosA = cos(rad).toFloat()
            val sinA = sin(rad).toFloat()
            val isMajor = i % (minorPerMajor + 1) == 0
            val inner = if (isMajor) majorInner else minorInner
            val paint = if (isMajor) majorTickPaint else minorTickPaint
            canvas.drawLine(
                cx + cosA * outer,
                cy + sinA * outer,
                cx + cosA * inner,
                cy + sinA * inner,
                paint,
            )
            if (isMajor) {
                val label = (maxValue * i / totalTicks).toString()
                val fm = labelPaint.fontMetrics
                val ly = cy + sinA * labelR - (fm.ascent + fm.descent) / 2f
                canvas.drawText(label, cx + cosA * labelR, ly, labelPaint)
            }
        }
    }

    private fun drawPointer(canvas: Canvas) {
        val angle = startAngle + value / maxValue * sweepAngle
        val rad = Math.toRadians(angle.toDouble())
        val tipR = radius - dp(30f)
        val tailR = dp(18f)
        val tipX = cx + cos(rad).toFloat() * tipR
        val tipY = cy + sin(rad).toFloat() * tipR
        val tailX = cx - cos(rad).toFloat() * tailR
        val tailY = cy - sin(rad).toFloat() * tailR
        canvas.drawLine(tailX, tailY, tipX, tipY, pointerPaint)
        canvas.drawCircle(cx, cy, dp(7f), hubPaint)
    }

    private fun drawText(canvas: Canvas) {
        val fm = valuePaint.fontMetrics
        val baseline = cy + radius * 0.35f - (fm.ascent + fm.descent) / 2f
        canvas.drawText(value.toInt().toString(), cx, baseline, valuePaint)
        canvas.drawText("km/h", cx, baseline + dp(20f), unitPaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
