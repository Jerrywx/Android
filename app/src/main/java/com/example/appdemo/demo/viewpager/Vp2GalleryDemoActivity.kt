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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.math.max

class Vp2GalleryDemoActivity : AppCompatActivity() {

    private enum class Mode { DEPTH, ZOOM, PARALLAX }

    private data class Card(val title: String, val sub: String, val tag: String, val emoji: String, val start: Int, val end: Int)

    private val items = listOf(
        Card("Compose", "声明式 UI 实战", "热门", "🎨", 0xFF6A8DFF.toInt(), 0xFF8E66FF.toInt()),
        Card("协程", "结构化并发", "进阶", "🧵", 0xFFFF8A65.toInt(), 0xFFFF5E8C.toInt()),
        Card("动画", "MotionLayout 入门", "动效", "🎞", 0xFF22C1C3.toInt(), 0xFF3CCB8A.toInt()),
        Card("性能", "启动 / 渲染 / 内存", "实战", "🚀", 0xFFFFB94B.toInt(), 0xFFFF7E5F.toInt()),
        Card("Material 3", "动态取色", "设计", "🌈", 0xFFA18CD1.toInt(), 0xFFFBC2EB.toInt()),
        Card("Navigation", "单 Activity 架构", "推荐", "🧭", 0xFF4FACFE.toInt(), 0xFF00F2FE.toInt()),
    )

    private var mode = Mode.DEPTH
    private lateinit var pager: ViewPager2
    private lateinit var dots: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vp2_gallery)
        setupDemoToolbar(R.string.vp2_gallery_title)

        pager = findViewById(R.id.vp2_gallery_pager)
        dots = findViewById(R.id.vp2_gallery_dots)
        val depth = findViewById<TextView>(R.id.vp2_gallery_mode_depth)
        val zoom = findViewById<TextView>(R.id.vp2_gallery_mode_zoom)
        val parallax = findViewById<TextView>(R.id.vp2_gallery_mode_parallax)

        pager.adapter = GalleryAdapter(items)
        pager.offscreenPageLimit = 3
        (pager.getChildAt(0) as RecyclerView).apply {
            clipToPadding = false
            clipChildren = false
            val pad = (32 * resources.displayMetrics.density).toInt()
            setPadding(pad, 0, pad, 0)
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        buildDots()
        applyMode(Mode.DEPTH, depth, zoom, parallax)

        depth.setOnClickListener { applyMode(Mode.DEPTH, depth, zoom, parallax) }
        zoom.setOnClickListener { applyMode(Mode.ZOOM, depth, zoom, parallax) }
        parallax.setOnClickListener { applyMode(Mode.PARALLAX, depth, zoom, parallax) }

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updateDots(position)
        })
    }

    private fun applyMode(m: Mode, depth: TextView, zoom: TextView, parallax: TextView) {
        mode = m
        depth.isSelected = m == Mode.DEPTH
        zoom.isSelected = m == Mode.ZOOM
        parallax.isSelected = m == Mode.PARALLAX
        pager.setPageTransformer(makeTransformer(m))
    }

    private fun makeTransformer(m: Mode): ViewPager2.PageTransformer = ViewPager2.PageTransformer { page, position ->
        val w = page.width.toFloat()
        when (m) {
            Mode.DEPTH -> {
                when {
                    position < -1 -> page.alpha = 0f
                    position <= 0 -> {
                        page.alpha = 1f
                        page.translationX = 0f
                        page.scaleX = 1f
                        page.scaleY = 1f
                    }
                    position <= 1 -> {
                        page.alpha = 1f - position
                        page.translationX = -w * position
                        val s = 0.75f + (1 - 0.75f) * (1 - abs(position))
                        page.scaleX = s
                        page.scaleY = s
                    }
                    else -> page.alpha = 0f
                }
            }
            Mode.ZOOM -> {
                val abs = abs(position)
                val scale = max(0.82f, 1f - abs * 0.18f)
                page.scaleX = scale
                page.scaleY = scale
                page.alpha = max(0.5f, 1f - abs * 0.5f)
                page.translationX = 0f
            }
            Mode.PARALLAX -> {
                page.translationX = 0f
                page.scaleX = 1f
                page.scaleY = 1f
                page.alpha = 1f - abs(position).coerceAtMost(1f) * 0.25f
                val emoji = page.findViewById<TextView>(R.id.vp2_gallery_emoji)
                val title = page.findViewById<TextView>(R.id.vp2_gallery_title)
                val sub = page.findViewById<TextView>(R.id.vp2_gallery_sub)
                emoji?.translationX = -position * w * 0.45f
                title?.translationX = -position * w * 0.25f
                sub?.translationX = -position * w * 0.15f
            }
        }
    }

    private fun buildDots() {
        dots.removeAllViews()
        val d = resources.displayMetrics.density
        items.forEachIndexed { i, _ ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams((8 * d).toInt(), (8 * d).toInt()).apply {
                    if (i != 0) marginStart = (8 * d).toInt()
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(if (i == 0) 0xFF07C160.toInt() else 0xFFD0D5DC.toInt())
                }
            }
            dots.addView(dot)
        }
    }

    private fun updateDots(selected: Int) {
        for (i in 0 until dots.childCount) {
            val dot = dots.getChildAt(i)
            val on = i == selected
            dot.animate().cancel()
            dot.animate()
                .scaleX(if (on) 2.4f else 1f)
                .scaleY(if (on) 1f else 1f)
                .setDuration(200).start()
            (dot.background as? GradientDrawable)?.setColor(
                if (on) 0xFF07C160.toInt() else 0xFFD0D5DC.toInt()
            )
        }
    }

    private class GalleryAdapter(val items: List<Card>) : RecyclerView.Adapter<GalleryAdapter.VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vp2_gallery, parent, false)
            return VH(v)
        }
        override fun getItemCount(): Int = items.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            private val card: FrameLayout = v.findViewById(R.id.vp2_gallery_card)
            private val tag: TextView = v.findViewById(R.id.vp2_gallery_tag)
            private val emoji: TextView = v.findViewById(R.id.vp2_gallery_emoji)
            private val title: TextView = v.findViewById(R.id.vp2_gallery_title)
            private val sub: TextView = v.findViewById(R.id.vp2_gallery_sub)
            fun bind(c: Card) {
                tag.text = c.tag
                emoji.text = c.emoji
                title.text = c.title
                sub.text = c.sub
                val d = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(c.start, c.end))
                d.cornerRadius = 28f * card.resources.displayMetrics.density
                card.background = d
            }
        }
    }
}
