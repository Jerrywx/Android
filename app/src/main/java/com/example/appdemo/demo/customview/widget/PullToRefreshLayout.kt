package com.example.appdemo.demo.customview.widget

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.abs
import kotlin.math.max

/**
 * 下拉刷新容器。
 *
 * 只包含两个子 View：[0] 头部 headerView，[1] 内容 contentView。
 *
 * 关键点：
 *   · onInterceptTouchEvent：内容在顶部且手指向下滑时才拦截，避免和内部滚动冲突
 *   · ACTION_MOVE：带阻尼下拉头部 offset，头部 translationY 同步
 *   · ACTION_UP：offset 越过阈值触发刷新，否则回弹
 *   · finishRefresh：外部调用，用 ValueAnimator 平滑收起头部
 */
class PullToRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    /** 状态回调：状态 + 当前下拉距离 (px) + 触发阈值 (px) */
    interface OnRefreshListener {
        fun onStateChanged(state: State, offset: Int, triggerDistance: Int)
        fun onRefresh()
    }

    enum class State { IDLE, PULL, RELEASE_TO_REFRESH, REFRESHING }

    private lateinit var headerView: View
    private lateinit var contentView: View

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downY = 0f
    private var lastY = 0f
    private var intercepting = false

    private var offset = 0
    private var headerHeight = 0
    private var triggerDistance = 0
    private var maxDistance = 0

    private var state: State = State.IDLE
    private var listener: OnRefreshListener? = null
    private var animator: ValueAnimator? = null

    fun setOnRefreshListener(l: OnRefreshListener) { listener = l }

    /** 通知外部刷新完成，头部收起 */
    fun finishRefresh() {
        if (state != State.REFRESHING) return
        transitionTo(State.IDLE)
        animateOffsetTo(0)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        require(childCount == 2) { "PullToRefreshLayout 需要恰好两个子 View：header + content" }
        headerView = getChildAt(0)
        contentView = getChildAt(1)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        headerHeight = headerView.measuredHeight
        if (headerHeight > 0) {
            triggerDistance = headerHeight
            maxDistance = (headerHeight * 2.2f).toInt()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        /// 头部默认藏在 content 上方
        headerView.layout(
            headerView.left,
            -headerHeight,
            headerView.right,
            0,
        )
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (state == State.REFRESHING) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downY = event.y
                lastY = event.y
                intercepting = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = event.y - downY
                /// 内容处于顶部 + 手指向下拉 + 位移超过 slop 才拦截
                if (!intercepting && dy > touchSlop && !contentView.canScrollVertically(-1)) {
                    intercepting = true
                    return true
                }
            }
        }
        return intercepting
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val dy = event.y - lastY
                lastY = event.y
                if (dy > 0 || offset > 0) {
                    /// 阻尼：offset 越大拉动越慢
                    val damping = 1f - (offset.toFloat() / maxDistance).coerceIn(0f, 0.8f)
                    val newOffset = (offset + dy * damping).toInt().coerceIn(0, maxDistance)
                    updateOffset(newOffset)
                    val next = when {
                        newOffset >= triggerDistance -> State.RELEASE_TO_REFRESH
                        newOffset > 0 -> State.PULL
                        else -> State.IDLE
                    }
                    transitionTo(next)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                intercepting = false
                if (offset >= triggerDistance) {
                    transitionTo(State.REFRESHING)
                    animateOffsetTo(triggerDistance)
                    listener?.onRefresh()
                } else {
                    animateOffsetTo(0)
                    transitionTo(State.IDLE)
                }
            }
        }
        return true
    }

    private fun animateOffsetTo(target: Int) {
        animator?.cancel()
        animator = ValueAnimator.ofInt(offset, target).apply {
            duration = 260
            addUpdateListener { updateOffset(it.animatedValue as Int) }
            start()
        }
    }

    private fun updateOffset(newOffset: Int) {
        offset = newOffset
        headerView.translationY = offset.toFloat()
        contentView.translationY = offset.toFloat()
        listener?.onStateChanged(state, offset, max(1, triggerDistance))
    }

    private fun transitionTo(next: State) {
        if (next == state) return
        state = next
        listener?.onStateChanged(state, offset, max(1, triggerDistance))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
