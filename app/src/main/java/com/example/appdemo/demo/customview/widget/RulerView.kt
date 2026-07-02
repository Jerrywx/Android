package com.example.appdemo.demo.customview.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 横向刻度尺。
 *
 * · 一个"逻辑刻度值"（如身高 30..220），每格代表 step
 * · 每格宽度 tickGap 像素；总内容宽度 = (maxValue - minValue) * tickGap
 * · scrollX 表示当前中心线对应的像素偏移
 * · 触摸拖动：ACTION_MOVE 调整 scrollX，边界 clamp
 * · 抬手：VelocityTracker 拿速度，Scroller fling，停下时吸附到最近整格
 * · 长刻度每 tickPerLabel 格一根 + 数字标签
 *
 * 关键点：pixel <-> value 之间的换算集中在 pixelToValue / valueToPixel。
 */
class RulerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val tickGap = dp(8f)
    private val tickPerLabel = 10
    private val shortTickLen = dp(10f)
    private val longTickLen = dp(18f)

    private val minValue = 30
    private val maxValue = 220

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#88000000")
        strokeWidth = dp(1f)
    }
    private val longTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#222222")
        strokeWidth = dp(1.5f)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        textAlign = Paint.Align.CENTER
        textSize = dp(12f)
    }
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF1E88E5")
        strokeWidth = dp(2.5f)
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#222222")
        textAlign = Paint.Align.CENTER
        textSize = dp(22f)
        isFakeBoldText = true
    }
    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#888888")
        textSize = dp(12f)
    }

    /** 当前偏移量：0 对应 minValue，contentWidth 对应 maxValue */
    private var scrollOffset = 0f
    private val contentWidth: Float get() = (maxValue - minValue) * tickGap

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val minVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val maxVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity
    private var lastX = 0f
    private var downX = 0f
    private var dragging = false

    private var velocityTracker: VelocityTracker? = null
    private val scroller = Scroller(context)
    private var snapAnimator: ValueAnimator? = null

    private var listener: ((Int) -> Unit)? = null

    fun setOnValueChangeListener(l: (Int) -> Unit) { listener = l }

    fun setValue(value: Int, animate: Boolean = true) {
        val target = valueToOffset(value.coerceIn(minValue, maxValue))
        if (animate) animateOffsetTo(target) else {
            scrollOffset = target
            invalidate()
            notifyValue()
        }
    }

    fun getValue(): Int = offsetToValue(scrollOffset)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        /// 默认停在中间值
        scrollOffset = valueToOffset((minValue + maxValue) / 2)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val tracker = velocityTracker ?: VelocityTracker.obtain().also { velocityTracker = it }
        tracker.addMovement(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                scroller.forceFinished(true)
                snapAnimator?.cancel()
                lastX = event.x
                downX = event.x
                dragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastX
                if (!dragging && abs(event.x - downX) > touchSlop) dragging = true
                if (dragging) {
                    lastX = event.x
                    /// 手指右滑 → 值变小 → offset 减小
                    scrollOffset = (scrollOffset - dx).coerceIn(0f, contentWidth)
                    invalidate()
                    notifyValue()
                }
            }
            MotionEvent.ACTION_UP -> {
                tracker.computeCurrentVelocity(1000, maxVelocity.toFloat())
                val vx = tracker.xVelocity
                if (dragging && abs(vx) > minVelocity) {
                    scroller.fling(
                        scrollOffset.toInt(), 0,
                        -vx.toInt(), 0,
                        0, contentWidth.toInt(),
                        0, 0,
                    )
                    postInvalidateOnAnimation()
                } else {
                    snapToNearest()
                }
                recycleTracker()
            }
            MotionEvent.ACTION_CANCEL -> {
                snapToNearest()
                recycleTracker()
            }
        }
        return true
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollOffset = scroller.currX.toFloat().coerceIn(0f, contentWidth)
            invalidate()
            notifyValue()
            if (scroller.isFinished) snapToNearest()
        }
    }

    private fun snapToNearest() {
        val target = (scrollOffset / tickGap).roundToInt() * tickGap
        if (abs(target - scrollOffset) < 0.5f) return
        animateOffsetTo(target)
    }

    private fun animateOffsetTo(target: Float) {
        snapAnimator?.cancel()
        snapAnimator = ValueAnimator.ofFloat(scrollOffset, target).apply {
            duration = 200
            addUpdateListener {
                scrollOffset = it.animatedValue as Float
                invalidate()
                notifyValue()
            }
            start()
        }
    }

    private fun notifyValue() {
        listener?.invoke(getValue())
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val centerX = w / 2f
        val topOffset = dp(24f)

        /// 当前中心对应的刻度索引（可能是小数，绘制时向左向右扩展）
        val centerTick = scrollOffset / tickGap
        val visibleTicks = (w / tickGap).toInt() / 2 + 2

        val startIndex = (centerTick - visibleTicks).toInt().coerceAtLeast(0)
        val endIndex = (centerTick + visibleTicks).toInt().coerceAtMost(maxValue - minValue)

        for (i in startIndex..endIndex) {
            val x = centerX + (i * tickGap - scrollOffset)
            val isLong = i % tickPerLabel == 0
            if (isLong) {
                canvas.drawLine(x, topOffset, x, topOffset + longTickLen, longTickPaint)
                canvas.drawText("${minValue + i}", x, topOffset + longTickLen + dp(16f), labelPaint)
            } else {
                canvas.drawLine(x, topOffset, x, topOffset + shortTickLen, tickPaint)
            }
        }

        /// 中心指示线
        canvas.drawLine(centerX, topOffset - dp(4f), centerX, topOffset + longTickLen + dp(4f), indicatorPaint)

        /// 当前值 + 单位
        val value = getValue()
        val label = "$value"
        val fm = valuePaint.fontMetrics
        val baseline = h - dp(12f)
        canvas.drawText(label, centerX - dp(10f), baseline, valuePaint)
        canvas.drawText("cm", centerX + valuePaint.measureText(label) / 2f + dp(4f), baseline, unitPaint)
    }

    private fun offsetToValue(offset: Float): Int {
        val idx = (offset / tickGap).roundToInt()
        return (minValue + idx).coerceIn(minValue, maxValue)
    }

    private fun valueToOffset(value: Int): Float = (value - minValue) * tickGap

    private fun recycleTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
        dragging = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scroller.forceFinished(true)
        snapAnimator?.cancel()
        recycleTracker()
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
