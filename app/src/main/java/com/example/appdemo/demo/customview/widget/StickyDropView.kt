package com.example.appdemo.demo.customview.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * 粘性水滴。
 *
 * · 两个圆：固定圆 fixed（消息角标的位置），可拖圆 drag（手指跟随）
 * · 未断裂时用两条对称的二阶贝塞尔在两圆之间连出"腰身"，形成粘连
 * · 拖动距离超过 maxDistance 判定断裂，此后只画 drag 圆
 * · 抬手：未断裂 → 弹性回到 fixed；已断裂 → 触发 onBurst 回调（这里让 drag 消失）
 *
 * 几何：
 *   d       两圆圆心距
 *   r1/r2   两圆半径（半径随距离线性收缩，模拟被拉细）
 *   sinθ = (r1 - r2) / d，cosθ = ±√(1 - sin²θ)
 *   在两圆上分别求 4 个切点，控制点取两圆心的中点
 */
class StickyDropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF4D6D")
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = dp(12f)
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val path = Path()

    private val fixedRadius = dp(18f)
    private val dragRadius = dp(18f)
    private val maxDistance = dp(140f)

    private var fixedX = 0f
    private var fixedY = 0f
    private var dragX = 0f
    private var dragY = 0f

    private var burst = false
    private var visibleFlag = true

    /** 断裂或松手回到原位后的回调，比如让业务清红点 */
    var onBurst: (() -> Unit)? = null

    fun reset() {
        burst = false
        visibleFlag = true
        dragX = fixedX
        dragY = fixedY
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fixedX = w * 0.75f
        fixedY = h * 0.35f
        dragX = fixedX
        dragY = fixedY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!visibleFlag) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val d = hypot(event.x - fixedX, event.y - fixedY)
                /// 命中固定圆才拦截
                if (d > fixedRadius * 1.8f) return false
                dragX = event.x
                dragY = event.y
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                dragX = event.x
                dragY = event.y
                if (!burst && distance() > maxDistance) burst = true
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (burst) {
                    visibleFlag = false
                    invalidate()
                    onBurst?.invoke()
                } else {
                    animateBack()
                }
            }
        }
        return true
    }

    private fun animateBack() {
        val startX = dragX
        val startY = dragY
        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 320
            interpolator = OvershootInterpolator(3f)
            addUpdateListener {
                val t = it.animatedValue as Float
                dragX = fixedX + (startX - fixedX) * t
                dragY = fixedY + (startY - fixedY) * t
                invalidate()
            }
            start()
        }
    }

    private fun distance(): Float = hypot(dragX - fixedX, dragY - fixedY)

    override fun onDraw(canvas: Canvas) {
        if (!visibleFlag) return

        val d = distance()
        /// 断裂或拉得很近 → 直接画两圆，不画连接
        if (burst || d < dp(1f)) {
            if (!burst) canvas.drawCircle(fixedX, fixedY, fixedRadius, paint)
            canvas.drawCircle(dragX, dragY, dragRadius, paint)
            drawBadge(canvas, dragX, dragY)
            return
        }

        /// 半径随距离线性收缩（最多缩到 55%）
        val shrink = 1f - (d / maxDistance).coerceIn(0f, 0.45f)
        val r1 = fixedRadius * shrink
        val r2 = dragRadius

        val sinTheta = (r1 - r2) / d
        val cosTheta = kotlin.math.sqrt(1f - sinTheta * sinTheta)
        val angle = atan2(dragY - fixedY, dragX - fixedX)

        /// 两圆切点：分别在圆的"上下"两侧
        val p1x = fixedX + r1 * (sinTheta * cos(angle) - cosTheta * sin(angle))
        val p1y = fixedY + r1 * (sinTheta * sin(angle) + cosTheta * cos(angle))
        val p2x = fixedX + r1 * (sinTheta * cos(angle) + cosTheta * sin(angle))
        val p2y = fixedY + r1 * (sinTheta * sin(angle) - cosTheta * cos(angle))
        val p3x = dragX + r2 * (sinTheta * cos(angle) - cosTheta * sin(angle))
        val p3y = dragY + r2 * (sinTheta * sin(angle) + cosTheta * cos(angle))
        val p4x = dragX + r2 * (sinTheta * cos(angle) + cosTheta * sin(angle))
        val p4y = dragY + r2 * (sinTheta * sin(angle) - cosTheta * cos(angle))

        val cx = (fixedX + dragX) / 2f
        val cy = (fixedY + dragY) / 2f

        path.reset()
        path.moveTo(p1x, p1y)
        path.quadTo(cx, cy, p3x, p3y)
        path.lineTo(p4x, p4y)
        path.quadTo(cx, cy, p2x, p2y)
        path.close()

        canvas.drawPath(path, paint)
        canvas.drawCircle(fixedX, fixedY, r1, paint)
        canvas.drawCircle(dragX, dragY, r2, paint)
        drawBadge(canvas, dragX, dragY)
    }

    private fun drawBadge(canvas: Canvas, cx: Float, cy: Float) {
        val fm = textPaint.fontMetrics
        val baseline = cy - (fm.ascent + fm.descent) / 2f
        canvas.drawText("99+", cx, baseline, textPaint)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
