package com.example.appdemo.demo.media.lrc

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import kotlin.math.abs

/**
 * LRC 歌词滚动视图。
 *
 *   · 当前行居中、放大、高亮；其它行小字灰色
 *   · 播放中：跟随进度平滑滚动到当前行
 *   · 用户拖动：暂停自动跟随，显示"跳到这一行"的指示线
 *   · 松手 5s 内无操作 → 恢复跟随；点击指示行 → 回调 seek 到该行时间
 *
 * 只画文字与指示线，滚动由自己的 offset + OverScroller 完成，不套 ScrollView。
 */
class LrcView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val activePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF6D00.toInt()
        textSize = sp(19f)
        isFakeBoldText = true
    }
    private val normalPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF9AA0A6.toInt()
        textSize = sp(15f)
    }
    private val hintPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB0B0B0.toInt()
        textSize = sp(13f)
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x33FF6D00
        strokeWidth = dp(1f)
    }

    private val lineGap: Float = dp(18f)

    private var lines: List<LrcParser.LrcLine> = emptyList()
    private var currentIndex: Int = -1

    /** 当前滚动偏移；0 表示第 0 行居中 */
    private var offsetY: Float = 0f

    /** 用户拖拽中 */
    private var isDragging = false

    /** 惯性滚动中 */
    private var isFlinging = false

    /** 用户抬手后残留的"拖拽状态"过期时间戳(SystemClock.uptimeMillis 域) */
    private var dragExpireAt = 0L

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val minFlingVel = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val maxFlingVel = ViewConfiguration.get(context).scaledMaximumFlingVelocity
    private val scroller = OverScroller(context)
    private var tracker: VelocityTracker? = null
    private var lastY = 0f
    private var downY = 0f
    private var downTime = 0L

    private var seekListener: ((Long) -> Unit)? = null

    private val autoResumeRunnable = Runnable {
        isDragging = false
        smoothScrollToIndex(currentIndex)
    }

    fun setLines(lines: List<LrcParser.LrcLine>) {
        this.lines = lines
        currentIndex = -1
        offsetY = 0f
        scroller.forceFinished(true)
        removeCallbacks(autoResumeRunnable)
        isDragging = false
        invalidate()
    }

    fun setOnSeekListener(l: (Long) -> Unit) {
        seekListener = l
    }

    /**
     * 播放器给的当前行索引;-1 表示还没到第一句。
     * 只在非拖拽状态下才平滑滚动跟随。
     */
    fun updateProgress(index: Int) {
        if (index == currentIndex) return
        currentIndex = index
        if (!isDragging && !isFlinging) {
            smoothScrollToIndex(index)
        } else {
            invalidate()
        }
    }

    private fun smoothScrollToIndex(index: Int) {
        val target = indexToOffset(index)
        val dy = (target - offsetY).toInt()
        if (dy == 0) return
        scroller.startScroll(0, offsetY.toInt(), 0, dy, 400)
        invalidate()
    }

    private fun indexToOffset(index: Int): Float {
        if (index < 0) return 0f
        return index * lineHeight()
    }

    private fun offsetToIndex(offset: Float): Int {
        if (lines.isEmpty()) return -1
        val raw = (offset / lineHeight()).toInt()
        return raw.coerceIn(0, lines.size - 1)
    }

    private fun lineHeight(): Float = normalPaint.textSize + lineGap

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            offsetY = scroller.currY.toFloat()
            if (scroller.isFinished) {
                isFlinging = false
            }
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (lines.isEmpty()) {
            val hint = "等待歌词…"
            val w = hintPaint.measureText(hint)
            canvas.drawText(hint, (width - w) / 2f, height / 2f, hintPaint)
            return
        }

        val centerY = height / 2f
        val lh = lineHeight()

        for (i in lines.indices) {
            val y = centerY + (i * lh) - offsetY
            if (y < -lh || y > height + lh) continue
            val paint = if (i == currentIndex) activePaint else normalPaint
            val text = lines[i].text.ifEmpty { "♪" }
            val w = paint.measureText(text)
            canvas.drawText(text, (width - w) / 2f, y + paint.textSize / 3f, paint)
        }

        if (isDragging || isFlinging) {
            canvas.drawLine(dp(24f), centerY, width - dp(24f), centerY, linePaint)
            val idx = offsetToIndex(offsetY)
            val ts = lines[idx].timeMs
            val label = formatTime(ts)
            canvas.drawText(label, width - hintPaint.measureText(label) - dp(12f), centerY - dp(6f), hintPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (lines.isEmpty()) return false
        val tracker = tracker ?: VelocityTracker.obtain().also { tracker = it }
        tracker.addMovement(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                scroller.forceFinished(true)
                lastY = event.y
                downY = event.y
                downTime = event.eventTime
                removeCallbacks(autoResumeRunnable)
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = lastY - event.y
                if (!isDragging && abs(event.y - downY) > touchSlop) {
                    isDragging = true
                }
                if (isDragging) {
                    val max = (lines.size - 1) * lineHeight()
                    offsetY = (offsetY + dy).coerceIn(-lineHeight(), max + lineHeight())
                    invalidate()
                }
                lastY = event.y
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    tracker.computeCurrentVelocity(1000, maxFlingVel.toFloat())
                    val vy = -tracker.yVelocity
                    if (abs(vy) > minFlingVel) {
                        isFlinging = true
                        val max = ((lines.size - 1) * lineHeight()).toInt()
                        scroller.fling(0, offsetY.toInt(), 0, vy.toInt(), 0, 0, 0, max)
                        invalidate()
                    }
                    postDelayed(autoResumeRunnable, RESUME_DELAY_MS)
                } else if (event.eventTime - downTime < ViewConfiguration.getTapTimeout() + 100 &&
                    abs(event.y - downY) < touchSlop
                ) {
                    // tap → seek 到当前指示行
                    val idx = offsetToIndex(offsetY)
                    seekListener?.invoke(lines[idx].timeMs)
                    isDragging = false
                }
                recycleTracker()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (isDragging) postDelayed(autoResumeRunnable, RESUME_DELAY_MS)
                recycleTracker()
            }
        }
        return true
    }

    private fun recycleTracker() {
        tracker?.recycle()
        tracker = null
    }

    private fun formatTime(ms: Long): String {
        val total = ms / 1000
        return "%02d:%02d".format(total / 60, total % 60)
    }

    private fun sp(v: Float): Float = v * resources.displayMetrics.scaledDensity
    private fun dp(v: Float): Float = v * resources.displayMetrics.density

    companion object {
        private const val RESUME_DELAY_MS = 3500L
    }
}
