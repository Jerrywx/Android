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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.max

class Vp2CardStackDemoActivity : AppCompatActivity() {

    private data class Card(
        val title: String,
        val sub: String,
        val tag: String,
        val emoji: String,
        val meta: String,
        val start: Int,
        val end: Int,
    )

    private val items = listOf(
        Card("Compose", "声明式 UI 实战", "热门", "🎨", "168 节 · 22 小时", 0xFF6A8DFF.toInt(), 0xFF8E66FF.toInt()),
        Card("协程进阶", "Flow / StateFlow / 共享流", "进阶", "🧵", "96 节 · 14 小时", 0xFFFF8A65.toInt(), 0xFFFF5E8C.toInt()),
        Card("动画系统", "属性 / 转场 / MotionLayout", "动效", "🎞", "72 节 · 10 小时", 0xFF22C1C3.toInt(), 0xFF3CCB8A.toInt()),
        Card("性能优化", "启动 / 渲染 / 内存治理", "实战", "🚀", "54 节 · 8 小时", 0xFFFFB94B.toInt(), 0xFFFF7E5F.toInt()),
        Card("Material 3", "DynamicColor / Tokens", "设计", "🌈", "38 节 · 5 小时", 0xFFA18CD1.toInt(), 0xFFFBC2EB.toInt()),
        Card("Architecture", "单 Activity / 模块化", "推荐", "🧭", "112 节 · 17 小时", 0xFF4FACFE.toInt(), 0xFF00F2FE.toInt()),
    )

    private lateinit var pager: ViewPager2
    private lateinit var dots: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vp2_card_stack)
        setupDemoToolbar(R.string.vp2_card_title)

        pager = findViewById(R.id.vp2_card_pager)
        dots = findViewById(R.id.vp2_card_dots)

        pager.adapter = CardAdapter(items)
        pager.offscreenPageLimit = 3

        (pager.getChildAt(0) as RecyclerView).apply {
            clipChildren = false
            clipToPadding = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        // 卡片堆叠 PageTransformer：当前页居中放大，相邻页向后缩小淡出
        val density = resources.displayMetrics.density
        pager.setPageTransformer { page, position ->
            val abs = abs(position)
            val scale = max(0.86f, 1f - abs * 0.12f)
            page.scaleX = scale
            page.scaleY = scale
            page.alpha = max(0.55f, 1f - abs * 0.45f)
            // 让相邻卡片向中间靠拢，形成堆叠错位
            page.translationX = -position * 32f * density
            page.translationZ = -abs
        }

        buildDots()
        updateDots(0)
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updateDots(position)
        })
    }

    private fun buildDots() {
        val d = resources.displayMetrics.density
        items.forEachIndexed { i, _ ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams((8 * d).toInt(), (8 * d).toInt()).apply {
                    if (i != 0) marginStart = (8 * d).toInt()
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(0xFFD0D5DC.toInt())
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
            dot.animate().scaleX(if (on) 2.4f else 1f).scaleY(1f).setDuration(200).start()
            (dot.background as? GradientDrawable)?.setColor(
                if (on) 0xFF07C160.toInt() else 0xFFD0D5DC.toInt()
            )
        }
    }

    private class CardAdapter(val items: List<Card>) : RecyclerView.Adapter<CardAdapter.VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vp2_card, parent, false)
            return VH(v)
        }
        override fun getItemCount(): Int = items.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            private val root: FrameLayout = v.findViewById(R.id.vp2_card_root)
            private val tag: TextView = v.findViewById(R.id.vp2_card_tag)
            private val emoji: TextView = v.findViewById(R.id.vp2_card_emoji)
            private val title: TextView = v.findViewById(R.id.vp2_card_title)
            private val sub: TextView = v.findViewById(R.id.vp2_card_sub)
            private val meta: TextView = v.findViewById(R.id.vp2_card_meta)
            fun bind(c: Card) {
                tag.text = c.tag
                emoji.text = c.emoji
                title.text = c.title
                sub.text = c.sub
                meta.text = c.meta
                val d = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(c.start, c.end))
                d.cornerRadius = 28f * root.resources.displayMetrics.density
                root.background = d
            }
        }
    }
}
