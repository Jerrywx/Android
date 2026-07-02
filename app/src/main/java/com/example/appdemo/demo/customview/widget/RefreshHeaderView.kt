package com.example.appdemo.demo.customview.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView

/**
 * 默认下拉刷新头部。
 *
 * 布局：左侧一个箭头 TextView（旋转表示"松开刷新"），刷新中切换为 ProgressBar；
 * 右侧一段文案。为了不引入更多资源，箭头用 "↓" 文本代替，通过 rotation 表示朝向。
 */
class RefreshHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val arrow: TextView
    private val progress: ProgressBar
    private val text: TextView

    private var lastState: PullToRefreshLayout.State = PullToRefreshLayout.State.IDLE

    init {
        val density = resources.displayMetrics.density
        val height = (56 * density).toInt()
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, height)
        setBackgroundColor(Color.parseColor("#F5F7FB"))

        val iconSize = (24 * density).toInt()
        val gap = (12 * density).toInt()

        arrow = TextView(context).apply {
            layoutParams = LayoutParams(iconSize, iconSize, Gravity.CENTER).apply {
                marginEnd = (100 * density).toInt()
            }
            gravity = Gravity.CENTER
            text = "↓"
            textSize = 18f
            setTextColor(Color.parseColor("#666666"))
        }
        progress = ProgressBar(context).apply {
            layoutParams = LayoutParams(iconSize, iconSize, Gravity.CENTER).apply {
                marginEnd = (100 * density).toInt()
            }
            visibility = INVISIBLE
        }
        text = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER).apply {
                marginStart = iconSize + gap
            }
            setTextColor(Color.parseColor("#666666"))
            textSize = 13f
        }
        addView(arrow)
        addView(progress)
        addView(text)
        applyState(PullToRefreshLayout.State.IDLE, 0, 1)
    }

    /**
     * 根据当前状态 + 下拉进度更新头部展示。
     *
     * @param state 当前状态
     * @param offset 当前下拉距离 px
     * @param triggerDistance 触发刷新的阈值 px
     */
    fun applyState(state: PullToRefreshLayout.State, offset: Int, triggerDistance: Int) {
        if (state != lastState) {
            when (state) {
                PullToRefreshLayout.State.IDLE, PullToRefreshLayout.State.PULL -> {
                    text.text = context.getString(com.example.appdemo.R.string.customview_refresh_pull)
                    arrow.visibility = VISIBLE
                    progress.visibility = INVISIBLE
                }
                PullToRefreshLayout.State.RELEASE_TO_REFRESH -> {
                    text.text = context.getString(com.example.appdemo.R.string.customview_refresh_release)
                    arrow.visibility = VISIBLE
                    progress.visibility = INVISIBLE
                }
                PullToRefreshLayout.State.REFRESHING -> {
                    text.text = context.getString(com.example.appdemo.R.string.customview_refresh_loading)
                    arrow.visibility = INVISIBLE
                    progress.visibility = VISIBLE
                }
            }
            lastState = state
        }
        /// 拉动比例超过阈值时箭头翻转，视觉指示"松手就刷新"
        arrow.rotation = if (offset >= triggerDistance) 180f else 0f
    }
}
