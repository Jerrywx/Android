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
import android.view.animation.DecelerateInterpolator
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 星评分控件。
 *
 * · Path 缓存一颗五角星的形状，绘制时按索引平移复用
 * · 空心背景 → clipRect 裁剪出填充比例 → 再画一次实心星 → 得到半星精度
 * · 手指按下 / 拖动时按 x 坐标换算成 rating；抬手用 ValueAnimator 平滑到目标 rating（半星步进）
 */
class StarRatingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val starCount = 5
    private var starSize = dp(36f)
    private var starGap = dp(8f)

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#E0E0E0")
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFC107")
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
        color = Color.parseColor("#FFB300")
    }

    private val starPath = Path()

    /** 当前评分，0..starCount，步进 0.5 */
    var rating: Float = 0f
        private set

    private var listener: ((Float) -> Unit)? = null
    fun setOnRatingChanged(l: (Float) -> Unit) { listener = l }

    private var animator: ValueAnimator? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        /// 让 5 颗星 + 4 个间隙填满宽度
        val available = (w - paddingLeft - paddingRight).toFloat()
        starSize = ((available - starGap * (starCount - 1)) / starCount).coerceAtLeast(dp(24f))
        buildStarPath(starSize)
    }

    private fun buildStarPath(size: Float) {
        starPath.reset()
        val outerR = size / 2f
        val innerR = outerR * 0.5f
        val cx = size / 2f
        val cy = size / 2f
        /// 从顶点开始，10 个点交替 outer / inner
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = Math.toRadians((-90 + i * 36).toDouble())
            val px = cx + r * cos(angle).toFloat()
            val py = cy + r * sin(angle).toFloat()
            if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
        }
        starPath.close()
    }

    override fun onDraw(canvas: Canvas) {
        val top = (height - starSize) / 2f
        var left = paddingLeft.toFloat()

        for (i in 0 until starCount) {
            val fillPortion = (rating - i).coerceIn(0f, 1f)

            /// 空心星 + 描边
            canvas.save()
            canvas.translate(left, top)
            canvas.drawPath(starPath, emptyPaint)
            canvas.drawPath(starPath, strokePaint)
            canvas.restore()

            /// 填充部分（半星靠 clipRect 裁一半）
            if (fillPortion > 0) {
                canvas.save()
                canvas.translate(left, top)
                canvas.clipRect(0f, 0f, starSize * fillPortion, starSize)
                canvas.drawPath(starPath, fillPaint)
                canvas.restore()
            }

            left += starSize + starGap
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                animator?.cancel()
                rating = ratingFromX(event.x).coerceIn(0f, starCount.toFloat())
                listener?.invoke(rating)
                invalidate()
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                /// 抬手对齐到最近的半星
                val target = (rating * 2f).roundToInt() / 2f
                animateTo(target)
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setRating(value: Float, animate: Boolean = false) {
        val clamped = value.coerceIn(0f, starCount.toFloat())
        val snapped = (clamped * 2f).roundToInt() / 2f
        if (animate) {
            animateTo(snapped)
        } else {
            animator?.cancel()
            rating = snapped
            listener?.invoke(rating)
            invalidate()
        }
    }

    private fun animateTo(target: Float) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(rating, target).apply {
            duration = 220
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                rating = it.animatedValue as Float
                listener?.invoke(rating)
                invalidate()
            }
            start()
        }
    }

    private fun ratingFromX(x: Float): Float {
        val step = starSize + starGap
        val relX = x - paddingLeft
        if (relX <= 0) return 0f
        val idx = (relX / step).toInt().coerceAtMost(starCount - 1)
        val inStar = relX - idx * step
        val portion = (inStar / starSize).coerceIn(0f, 1f)
        return idx + portion
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
