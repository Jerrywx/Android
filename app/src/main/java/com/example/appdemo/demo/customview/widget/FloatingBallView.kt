package com.example.appdemo.demo.customview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.Scroller
import kotlin.math.abs

/**
 * 可拖拽悬浮球。
 *
 * · TouchSlop：过滤微小抖动，超过阈值才算拖动，避免误触
 * · VelocityTracker：抬手时读到 X/Y 方向速度
 * · Scroller + computeScroll：抛掷惯性滑行
 * · 松手后按停靠位置吸附到父容器左 / 右边缘
 */
class FloatingBallView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E88E5")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = dp(14f)
        isFakeBoldText = true
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33000000")
    }

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val minFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val maxFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity

    private val scroller = Scroller(context)
    private var velocityTracker: VelocityTracker? = null

    private var downRawX = 0f
    private var downRawY = 0f
    private var downLeft = 0
    private var downTop = 0
    private var isDragging = false

    var label: String = "拖我"
        set(value) { field = value; invalidate() }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(cx, cy) - dp(2f)
        /// 阴影稍微偏下
        canvas.drawCircle(cx, cy + dp(2f), radius, shadowPaint)
        canvas.drawCircle(cx, cy, radius, fillPaint)
        val fm = textPaint.fontMetrics
        val baseline = cy - (fm.ascent + fm.descent) / 2f
        canvas.drawText(label, cx, baseline, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val tracker = velocityTracker ?: VelocityTracker.obtain().also { velocityTracker = it }
        tracker.addMovement(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                /// 中断进行中的惯性
                if (!scroller.isFinished) scroller.abortAnimation()
                downRawX = event.rawX
                downRawY = event.rawY
                downLeft = left
                downTop = top
                isDragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY
                if (!isDragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                    isDragging = true
                }
                if (isDragging) {
                    moveTo((downLeft + dx).toInt(), (downTop + dy).toInt())
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    tracker.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                    val vx = tracker.xVelocity.toInt()
                    val vy = tracker.yVelocity.toInt()
                    if (abs(vx) > minFlingVelocity || abs(vy) > minFlingVelocity) {
                        fling(vx, vy)
                    } else {
                        snapToEdge()
                    }
                } else {
                    performClick()
                }
                recycleTracker()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            moveTo(scroller.currX, scroller.currY)
            postInvalidateOnAnimation()
            if (scroller.isFinished) snapToEdge()
        }
    }

    /**
     * 抛掷 —— 用 Scroller.fling 计算一次目标位置，边界收敛到父容器可用区域。
     */
    private fun fling(vx: Int, vy: Int) {
        val parent = parent as? ViewGroup ?: return
        val minX = 0
        val maxX = parent.width - width
        val minY = 0
        val maxY = parent.height - height
        scroller.fling(left, top, vx, vy, minX, maxX, minY, maxY)
        postInvalidateOnAnimation()
    }

    /**
     * 抬手 / 惯性结束后，用 Scroller.startScroll 平滑吸附到最近的一条边。
     */
    private fun snapToEdge() {
        val parent = parent as? ViewGroup ?: return
        val targetLeft = if (left + width / 2 < parent.width / 2) 0 else parent.width - width
        val targetTop = top.coerceIn(0, parent.height - height)
        val dx = targetLeft - left
        val dy = targetTop - top
        if (dx == 0 && dy == 0) return
        scroller.startScroll(left, top, dx, dy, 280)
        postInvalidateOnAnimation()
    }

    /**
     * 直接把 View 移动到父坐标 (x, y)，同时做边界约束。
     */
    private fun moveTo(x: Int, y: Int) {
        val parent = parent as? ViewGroup ?: return
        val clampedX = x.coerceIn(0, parent.width - width)
        val clampedY = y.coerceIn(0, parent.height - height)
        offsetLeftAndRight(clampedX - left)
        offsetTopAndBottom(clampedY - top)
    }

    fun moveToCenter() {
        val parent = parent as? ViewGroup ?: return
        if (!scroller.isFinished) scroller.abortAnimation()
        val targetLeft = (parent.width - width) / 2
        val targetTop = (parent.height - height) / 2
        val dx = targetLeft - left
        val dy = targetTop - top
        scroller.startScroll(left, top, dx, dy, 300)
        postInvalidateOnAnimation()
    }

    private fun recycleTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recycleTracker()
        if (!scroller.isFinished) scroller.abortAnimation()
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
