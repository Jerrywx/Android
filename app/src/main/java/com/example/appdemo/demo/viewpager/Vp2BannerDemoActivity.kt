package com.example.appdemo.demo.viewpager

import com.example.appdemo.R
import com.example.appdemo.common.*
import com.example.appdemo.tabs.*
import com.example.appdemo.demo.layout.*
import com.example.appdemo.demo.concurrent.*
import com.example.appdemo.demo.network.*
import com.example.appdemo.demo.animation.*
import com.example.appdemo.demo.animation.widget.*
import com.example.appdemo.demo.viewpager.*
import com.example.appdemo.demo.recyclerview.*
import com.example.appdemo.demo.recyclerview.chat.*
import com.example.appdemo.demo.fragment.*

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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class Vp2BannerDemoActivity : AppCompatActivity() {

    private data class Banner(val title: String, val sub: String, val tag: String, val emoji: String, val start: Int, val end: Int)
    private data class RecommendItem(val title: String, val sub: String, val meta: String, val emoji: String, val start: Int, val end: Int)

    private val banners = listOf(
        Banner("Compose", "声明式 UI 实战", "热门", "🎨", 0xFF6A8DFF.toInt(), 0xFF8E66FF.toInt()),
        Banner("协程进阶", "结构化并发", "进阶", "🧵", 0xFFFF8A65.toInt(), 0xFFFF5E8C.toInt()),
        Banner("动画系统", "MotionLayout 入门", "动效", "🎞", 0xFF22C1C3.toInt(), 0xFF3CCB8A.toInt()),
        Banner("性能优化", "启动 / 渲染 / 内存", "实战", "🚀", 0xFFFFB94B.toInt(), 0xFFFF7E5F.toInt()),
        Banner("Material 3", "动态取色", "设计", "🌈", 0xFFA18CD1.toInt(), 0xFFFBC2EB.toInt()),
    )

    private val recommends = listOf(
        RecommendItem("Compose State 全解", "三步搞懂 remember / derivedStateOf", "12 分钟", "📘", 0xFF6A8DFF.toInt(), 0xFF8E66FF.toInt()),
        RecommendItem("Flow vs LiveData", "选择指南 · 项目最佳实践", "10 分钟", "🌊", 0xFFFF8A65.toInt(), 0xFFFF5E8C.toInt()),
        RecommendItem("PageTransformer 实战", "深度 / 缩放 / 视差 三连击", "08 分钟", "🎬", 0xFF22C1C3.toInt(), 0xFF3CCB8A.toInt()),
        RecommendItem("启动优化清单", "冷启动 3s → 800ms 实战", "15 分钟", "🔥", 0xFFFFB94B.toInt(), 0xFFFF7E5F.toInt()),
        RecommendItem("依赖注入指南", "Hilt / Koin 你应该选谁", "11 分钟", "🧩", 0xFFA18CD1.toInt(), 0xFFFBC2EB.toInt()),
        RecommendItem("WorkManager 全景", "约束 / 链式 / 加急任务", "09 分钟", "⚙️", 0xFF4FACFE.toInt(), 0xFF00F2FE.toInt()),
    )

    private lateinit var pager: ViewPager2
    private lateinit var dots: LinearLayout
    private lateinit var status: TextView
    private lateinit var toggle: TextView
    private var autoPlay = true
    private val autoPlayDelay = 3000L
    private val handler = Handler(Looper.getMainLooper())
    private val autoPlayRunnable = object : Runnable {
        override fun run() {
            if (autoPlay && !pager.isFakeDragging) {
                pager.setCurrentItem(pager.currentItem + 1, true)
            }
            handler.postDelayed(this, autoPlayDelay)
        }
    }

    // 用大数字实现无限循环：起点定在 N * banners.size 中段
    private val virtualCount = banners.size * 400
    private val startIndex = virtualCount / 2 - (virtualCount / 2) % banners.size

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vp2_banner)
        setupDemoToolbar(R.string.vp2_banner_title)

        pager = findViewById(R.id.vp2_banner_pager)
        dots = findViewById(R.id.vp2_banner_dots)
        status = findViewById(R.id.vp2_banner_status)
        toggle = findViewById(R.id.vp2_banner_toggle)

        pager.adapter = BannerAdapter(banners, virtualCount)
        pager.offscreenPageLimit = 1
        pager.setCurrentItem(startIndex, false)

        // 给每页留出 16dp 的对称内边距，露出两侧前后页的边缘
        (pager.getChildAt(0) as RecyclerView).apply {
            clipToPadding = false
            clipChildren = false
            val pad = (12 * resources.displayMetrics.density).toInt()
            setPadding(pad, 0, pad, 0)
        }

        // 切换时附带轻微缩放，过渡更顺
        pager.setPageTransformer { page, position ->
            val abs = kotlin.math.abs(position)
            val scale = 1f - abs.coerceAtMost(1f) * 0.06f
            page.scaleX = scale
            page.scaleY = scale
        }

        buildDots()
        updateUi(startIndex)
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updateUi(position)
        })

        toggle.setOnClickListener {
            autoPlay = !autoPlay
            updateToggleText()
            if (autoPlay) scheduleAutoPlay() else handler.removeCallbacks(autoPlayRunnable)
        }
        updateToggleText()

        // 触摸期间暂停轮播，避免与用户手势冲突
        pager.getChildAt(0).setOnTouchListener { _, ev ->
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> handler.removeCallbacks(autoPlayRunnable)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    if (autoPlay) scheduleAutoPlay()
            }
            false
        }

        val list = findViewById<RecyclerView>(R.id.vp2_banner_recommend_list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = RecommendAdapter(recommends)
        list.isNestedScrollingEnabled = false
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
        handler.postDelayed(autoPlayRunnable, autoPlayDelay)
    }

    private fun updateUi(virtualPos: Int) {
        val real = ((virtualPos % banners.size) + banners.size) % banners.size
        status.text = getString(R.string.vp2_banner_status, real + 1, banners.size, banners[real].title)
        for (i in 0 until dots.childCount) {
            val dot = dots.getChildAt(i)
            val on = i == real
            dot.animate().cancel()
            dot.animate().scaleX(if (on) 2.4f else 1f).scaleY(1f).setDuration(200).start()
            (dot.background as? GradientDrawable)?.setColor(
                if (on) 0xFFFFFFFF.toInt() else 0x66FFFFFF
            )
        }
    }

    private fun updateToggleText() {
        toggle.text = if (autoPlay) getString(R.string.vp2_banner_pause)
        else getString(R.string.vp2_banner_resume)
    }

    private fun buildDots() {
        dots.removeAllViews()
        val d = resources.displayMetrics.density
        banners.forEachIndexed { i, _ ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams((6 * d).toInt(), (6 * d).toInt()).apply {
                    if (i != 0) marginStart = (6 * d).toInt()
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(0x66FFFFFF)
                }
            }
            dots.addView(dot)
        }
    }

    private class BannerAdapter(val items: List<Banner>, val virtualSize: Int) : RecyclerView.Adapter<BannerAdapter.VH>() {
        override fun getItemCount(): Int = virtualSize
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vp2_banner, parent, false)
            v.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            return VH(v)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position % items.size])
        }

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            private val card: FrameLayout = v.findViewById(R.id.vp2_banner_card)
            private val tag: TextView = v.findViewById(R.id.vp2_banner_tag)
            private val emoji: TextView = v.findViewById(R.id.vp2_banner_emoji)
            private val title: TextView = v.findViewById(R.id.vp2_banner_title)
            private val sub: TextView = v.findViewById(R.id.vp2_banner_sub)
            fun bind(b: Banner) {
                tag.text = b.tag
                emoji.text = b.emoji
                title.text = b.title
                sub.text = b.sub
                val d = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(b.start, b.end))
                d.cornerRadius = 18f * card.resources.displayMetrics.density
                card.background = d
            }
        }
    }

    private class RecommendAdapter(val items: List<RecommendItem>) : RecyclerView.Adapter<RecommendAdapter.VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vp2_banner_recommend, parent, false)
            return VH(v)
        }
        override fun getItemCount(): Int = items.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            private val cover: FrameLayout = v.findViewById(R.id.vp2_banner_rec_cover)
            private val emoji: TextView = v.findViewById(R.id.vp2_banner_rec_emoji)
            private val title: TextView = v.findViewById(R.id.vp2_banner_rec_title)
            private val sub: TextView = v.findViewById(R.id.vp2_banner_rec_sub)
            private val meta: TextView = v.findViewById(R.id.vp2_banner_rec_meta)
            fun bind(r: RecommendItem) {
                title.text = r.title
                sub.text = r.sub
                meta.text = r.meta
                emoji.text = r.emoji
                val d = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(r.start, r.end))
                d.cornerRadius = 14f * cover.resources.displayMetrics.density
                cover.background = d
            }
        }
    }
}
