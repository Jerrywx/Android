package com.example.appdemo

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

class LoopBannerDemoActivity : AppCompatActivity() {

    private val items = LoopBannerData.items()
    private lateinit var list: RecyclerView
    private lateinit var indicator: LinearLayout
    private lateinit var status: TextView
    private lateinit var toggle: TextView
    private val snapHelper = LinearSnapHelper()
    private val handler = Handler(Looper.getMainLooper())
    private var autoPlay = true
    private val autoPlayInterval = 3000L
    private val autoPlayRunnable = object : Runnable {
        override fun run() {
            if (!autoPlay) return
            val lm = list.layoutManager as LinearLayoutManager
            val current = lm.findFirstVisibleItemPosition()
            if (current == RecyclerView.NO_POSITION) {
                handler.postDelayed(this, autoPlayInterval)
                return
            }
            list.smoothScrollToPosition(current + 1)
            handler.postDelayed(this, autoPlayInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo_loop_banner)
        setupDemoToolbar(R.string.loop_banner_title)

        list = findViewById(R.id.loop_banner_list)
        indicator = findViewById(R.id.loop_banner_indicator)
        status = findViewById(R.id.loop_banner_status)
        toggle = findViewById(R.id.loop_banner_toggle)

        list.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        list.adapter = LoopBannerAdapter(items)
        snapHelper.attachToRecyclerView(list)

        // 起始位置设在 MAX/2 的 items.size 整数倍处，左右都能无限滑
        val start = Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2) % items.size
        list.scrollToPosition(start)
        updateStatus(0)
        buildIndicators()
        updateIndicator(0)

        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                val view = snapHelper.findSnapView(rv.layoutManager) ?: return
                val pos = rv.layoutManager?.getPosition(view) ?: return
                val real = pos % items.size
                updateStatus(real)
                updateIndicator(real)
            }
        })

        // 用户触摸时暂停自动轮播，松手后恢复
        list.setOnTouchListener { _, ev ->
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> handler.removeCallbacks(autoPlayRunnable)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (autoPlay) scheduleAutoPlay()
            }
            false
        }

        toggle.setOnClickListener {
            autoPlay = !autoPlay
            updateToggleText()
            if (autoPlay) scheduleAutoPlay() else handler.removeCallbacks(autoPlayRunnable)
        }
        updateToggleText()
    }

    override fun onResume() {
        super.onResume()
        if (autoPlay) scheduleAutoPlay()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(autoPlayRunnable)
    }

    private fun scheduleAutoPlay() {
        handler.removeCallbacks(autoPlayRunnable)
        handler.postDelayed(autoPlayRunnable, autoPlayInterval)
    }

    private fun updateToggleText() {
        toggle.text = if (autoPlay) getString(R.string.loop_banner_pause)
        else getString(R.string.loop_banner_resume)
    }

    private fun updateStatus(real: Int) {
        status.text = getString(R.string.loop_banner_status, real + 1, items.size, items[real].title)
    }

    private fun buildIndicators() {
        val density = resources.displayMetrics.density
        items.forEach { _ ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (6 * density).toInt(), (6 * density).toInt()
                ).apply { marginEnd = (6 * density).toInt() }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(0xFFFFFFFF.toInt())
                }
                alpha = 0.5f
            }
            indicator.addView(dot)
        }
    }

    private fun updateIndicator(real: Int) {
        for (i in 0 until indicator.childCount) {
            val dot = indicator.getChildAt(i)
            val on = i == real
            dot.animate().cancel()
            dot.animate()
                .alpha(if (on) 1f else 0.5f)
                .scaleX(if (on) 1.6f else 1f)
                .scaleY(if (on) 1.6f else 1f)
                .setDuration(180).start()
        }
    }
}

private class LoopBannerAdapter(val items: List<LoopBannerData.Item>) : RecyclerView.Adapter<LoopBannerAdapter.VH>() {
    // itemCount = Int.MAX_VALUE 实现"无限"列表
    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_demo_loop_banner, parent, false)
        v.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
        )
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        // 用 position % items.size 拿真实数据，position 不断增加但映射回原始下标
        holder.bind(items[position % items.size])
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val card: FrameLayout = view.findViewById(R.id.loop_banner_card)
        private val title: TextView = view.findViewById(R.id.loop_banner_title)
        private val subtitle: TextView = view.findViewById(R.id.loop_banner_subtitle)
        private val tag: TextView = view.findViewById(R.id.loop_banner_tag)
        fun bind(it: LoopBannerData.Item) {
            title.text = it.title
            subtitle.text = it.subtitle
            tag.text = it.tag
            val d = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(it.start, it.end))
            d.cornerRadius = 18f * card.resources.displayMetrics.density
            card.background = d
        }
    }
}

object LoopBannerData {
    data class Item(val title: String, val subtitle: String, val tag: String, val start: Int, val end: Int)
    fun items(): List<Item> = listOf(
        Item("Compose", "声明式 UI 实战", "热门", 0xFF6A8DFF.toInt(), 0xFF8E66FF.toInt()),
        Item("协程", "结构化并发", "进阶", 0xFFFF8A65.toInt(), 0xFFFF5E8C.toInt()),
        Item("动画", "MotionLayout 入门", "动效", 0xFF22C1C3.toInt(), 0xFF3CCB8A.toInt()),
        Item("性能", "启动 / 渲染 / 内存", "实战", 0xFFFFB94B.toInt(), 0xFFFF7E5F.toInt()),
        Item("Material 3", "动态取色", "设计", 0xFFA18CD1.toInt(), 0xFFFBC2EB.toInt()),
    )
}
