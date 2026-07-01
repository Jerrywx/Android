package com.example.appdemo.demo.recyclerview

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.max

class GalleryDemoActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var indicator: LinearLayout
    private val items = GalleryData.items()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo_gallery)
        setupDemoToolbar(R.string.gallery_title)

        list = findViewById(R.id.gallery_list)
        indicator = findViewById(R.id.gallery_indicator)

        list.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        list.adapter = GalleryAdapter(items)
        PagerSnapHelper().attachToRecyclerView(list)
        list.addItemDecoration(GalleryZoomDecoration())
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) = applyZoom(rv)
        })
        list.post { applyZoom(list) }

        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) updateIndicator()
            }
        })

        buildIndicators()
    }

    private fun buildIndicators() {
        val density = resources.displayMetrics.density
        items.forEachIndexed { i, _ ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams((8 * density).toInt(), (8 * density).toInt()).apply {
                    marginEnd = (6 * density).toInt()
                }
                background = circleDrawable(getColor(R.color.demo_hint))
                alpha = if (i == 0) 1f else 0.35f
            }
            indicator.addView(dot)
        }
    }

    private fun updateIndicator() {
        val lm = list.layoutManager as LinearLayoutManager
        val current = lm.findFirstCompletelyVisibleItemPosition()
            .takeIf { it >= 0 } ?: lm.findFirstVisibleItemPosition()
        for (i in 0 until indicator.childCount) {
            val dot = indicator.getChildAt(i)
            dot.animate().alpha(if (i == current) 1f else 0.35f)
                .scaleX(if (i == current) 1.3f else 1f)
                .scaleY(if (i == current) 1.3f else 1f)
                .setDuration(180).start()
        }
    }

    private fun applyZoom(rv: RecyclerView) {
        val center = (rv.width / 2f)
        for (i in 0 until rv.childCount) {
            val child = rv.getChildAt(i)
            val childCenter = (child.left + child.right) / 2f
            val ratio = 1f - (abs(center - childCenter) / rv.width).coerceIn(0f, 1f)
            val scale = 0.85f + 0.15f * ratio
            child.scaleX = scale
            child.scaleY = scale
            child.alpha = 0.5f + 0.5f * ratio
        }
    }

    private fun circleDrawable(color: Int): GradientDrawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.OVAL
        d.setColor(color)
        return d
    }
}

private class GalleryAdapter(val items: List<GalleryData.Item>) : RecyclerView.Adapter<GalleryAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_demo_gallery, parent, false)
        return VH(view)
    }
    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val card: FrameLayout = view.findViewById(R.id.gallery_card)
        private val title: TextView = view.findViewById(R.id.gallery_title)
        private val subtitle: TextView = view.findViewById(R.id.gallery_subtitle)
        private val tag: TextView = view.findViewById(R.id.gallery_tag)
        fun bind(it: GalleryData.Item) {
            title.text = it.title
            subtitle.text = it.subtitle
            tag.text = it.tag
            val d = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(it.start, it.end))
            d.cornerRadius = 22f * card.resources.displayMetrics.density
            card.background = d
        }
    }
}

private class GalleryZoomDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.setEmpty()
    }
}

object GalleryData {
    data class Item(val title: String, val subtitle: String, val tag: String, val start: Int, val end: Int)
    fun items(): List<Item> = listOf(
        Item("Compose", "声明式 UI 实战", "热门", 0xFF6A8DFF.toInt(), 0xFF8E66FF.toInt()),
        Item("协程", "结构化并发", "进阶", 0xFFFF8A65.toInt(), 0xFFFF5E8C.toInt()),
        Item("动画", "MotionLayout 入门", "动效", 0xFF22C1C3.toInt(), 0xFF3CCB8A.toInt()),
        Item("性能", "启动 / 渲染 / 内存", "实战", 0xFFFFB94B.toInt(), 0xFFFF7E5F.toInt()),
        Item("Material 3", "动态取色", "设计", 0xFFA18CD1.toInt(), 0xFFFBC2EB.toInt()),
    )
}
