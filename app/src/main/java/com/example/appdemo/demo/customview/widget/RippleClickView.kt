package com.example.appdemo.demo.customview.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.hypot

/**
 * 水波纹点击 View。
 *
 * · 每次点击在触点生成一个 [Ripple]，用同一个 ValueAnimator 驱动半径 0 → maxRadius、透明度 alphaStart → 0
 * · 多个波纹并存：每个 Ripple 自己维护动画状态，绘制时遍历列表 drawCircle
 * · 动画结束后自动从列表移除，避免 ripples 无限增长
 */
class RippleClickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1E88E5")
    }

    private val ripples = mutableListOf<Ripple>()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            spawnRipple(event.x, event.y)
            performClick()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun clear() {
        ripples.forEach { it.animator.cancel() }
        ripples.clear()
        invalidate()
    }

    private fun spawnRipple(x: Float, y: Float) {
        /// 最大半径 = 触点到四角的最远距离，保证扩散能覆盖整个 View
        val maxRadius = maxOf(
            hypot(x, y),
            hypot(width - x, y),
            hypot(x, height - y),
            hypot(width - x, height - y),
        )
        val ripple = Ripple(x, y, maxRadius)
        ripple.animator.addUpdateListener {
            val f = it.animatedFraction
            ripple.radius = maxRadius * f
            ripple.alpha = (0xAA * (1 - f)).toInt().coerceIn(0, 255)
            invalidate()
        }
        ripple.animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                ripples.remove(ripple)
                invalidate()
            }
        })
        ripples.add(ripple)
        ripple.animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        for (r in ripples) {
            paint.alpha = r.alpha
            canvas.drawCircle(r.x, r.y, r.radius, paint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ripples.forEach { it.animator.cancel() }
        ripples.clear()
    }

    private class Ripple(val x: Float, val y: Float, maxRadius: Float) {
        var radius: Float = 0f
        var alpha: Int = 0xAA
        val animator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 700
            interpolator = DecelerateInterpolator()
        }
    }
}
