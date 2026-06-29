package com.example.appdemo

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StickyHeaderDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo_sticky)
        setupDemoToolbar(R.string.sticky_title)

        val list = findViewById<RecyclerView>(R.id.sticky_list)
        list.layoutManager = LinearLayoutManager(this)
        val items = StickyData.items()
        list.adapter = StickyAdapter(items)
        list.addItemDecoration(StickyHeaderDecoration(items, this))
    }
}

data class StickyItem(val group: String, val title: String, val subtitle: String, val color: Int)

object StickyData {
    fun items(): List<StickyItem> {
        val groups = listOf(
            "今天" to listOf("启动会议", "代码评审", "性能调试", "周报草稿"),
            "昨天" to listOf("接口联调", "UI 切图", "数据校验", "回归测试", "客户演示"),
            "本周早些时候" to listOf("立项讨论", "技术调研", "需求拆解"),
            "上周" to listOf("版本上线", "复盘会", "数据复盘", "线上事故处理", "灰度策略"),
        )
        val palette = intArrayOf(
            0xFF6A8DFF.toInt(), 0xFFFF8A65.toInt(), 0xFF22C1C3.toInt(),
            0xFFFFB94B.toInt(), 0xFF8E66FF.toInt(), 0xFF3CCB8A.toInt(),
        )
        val list = mutableListOf<StickyItem>()
        var k = 0
        groups.forEach { (g, items) ->
            items.forEach { t ->
                list += StickyItem(g, t, "演示粘性 Header 自绘", palette[k++ % palette.size])
            }
        }
        return list
    }
}

private class StickyAdapter(val items: List<StickyItem>) : RecyclerView.Adapter<StickyAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_demo_sticky, parent, false)
        return VH(v)
    }
    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val avatar: TextView = view.findViewById(R.id.sticky_avatar)
        private val title: TextView = view.findViewById(R.id.sticky_title)
        private val subtitle: TextView = view.findViewById(R.id.sticky_subtitle)
        fun bind(item: StickyItem) {
            avatar.text = item.title.firstOrNull()?.toString().orEmpty()
            val d = GradientDrawable()
            d.shape = GradientDrawable.OVAL
            d.setColor(item.color)
            avatar.background = d
            title.text = item.title
            subtitle.text = item.subtitle
        }
    }
}

private class StickyHeaderDecoration(
    private val items: List<StickyItem>,
    ctx: android.content.Context,
) : RecyclerView.ItemDecoration() {

    private val density = ctx.resources.displayMetrics.density
    private val headerHeight = (32 * density)
    private val padding = (20 * density)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ctx.getColor(R.color.demo_sticky_header_bg)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ctx.getColor(R.color.demo_sticky_header_text)
        textSize = 12 * density
        isFakeBoldText = true
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val pos = parent.getChildAdapterPosition(view)
        if (pos == RecyclerView.NO_POSITION) return
        if (pos == 0 || items[pos].group != items[pos - 1].group) {
            outRect.top = headerHeight.toInt()
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.childCount == 0) return
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val pos = parent.getChildAdapterPosition(child)
            if (pos == RecyclerView.NO_POSITION) continue
            if (pos == 0 || items[pos].group != items[pos - 1].group) {
                val top = (child.top - headerHeight).coerceAtLeast(0f)
                drawHeader(c, parent, items[pos].group, top, child.top.toFloat())
            }
        }

        val firstVisible = (parent.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        if (firstVisible == RecyclerView.NO_POSITION) return
        val firstItem = items[firstVisible]
        val nextIdx = (firstVisible + 1).coerceAtMost(items.lastIndex)
        val pushBy = if (firstItem.group != items[nextIdx].group) {
            val nextChild = parent.findViewHolderForAdapterPosition(nextIdx)?.itemView
            val top = nextChild?.top ?: parent.height
            (top - headerHeight).coerceAtMost(0f)
        } else 0f
        drawHeader(c, parent, firstItem.group, pushBy.toFloat(), pushBy + headerHeight)
    }

    private fun drawHeader(c: Canvas, parent: RecyclerView, label: String, top: Float, bottom: Float) {
        c.drawRect(0f, top, parent.width.toFloat(), bottom, bgPaint)
        val textY = top + (bottom - top + textPaint.textSize) / 2f - textPaint.descent() / 2f
        c.drawText(label, padding, textY, textPaint)
    }
}
