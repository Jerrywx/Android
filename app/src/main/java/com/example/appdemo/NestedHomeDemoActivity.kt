package com.example.appdemo

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NestedHomeDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo_nested)
        setupDemoToolbar(R.string.nested_title)

        val list = findViewById<RecyclerView>(R.id.nested_list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = NestedSectionAdapter(NestedData.sections())
        list.setRecycledViewPool(RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(0, 12) })
    }
}

private class NestedSectionAdapter(val sections: List<NestedData.Section>) : RecyclerView.Adapter<NestedSectionAdapter.VH>() {
    private val sharedPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_demo_nested_section, parent, false)
        return VH(view, sharedPool)
    }
    override fun getItemCount(): Int = sections.size
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(sections[position])

    class VH(view: View, private val pool: RecyclerView.RecycledViewPool) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.nested_section_title)
        private val subtitle: TextView = view.findViewById(R.id.nested_section_subtitle)
        private val row: RecyclerView = view.findViewById(R.id.nested_section_row)
        init {
            row.setRecycledViewPool(pool)
            row.layoutManager = LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)
        }
        fun bind(section: NestedData.Section) {
            title.text = section.title
            subtitle.text = section.subtitle
            row.adapter = NestedCardAdapter(section.cards)
        }
    }
}

private class NestedCardAdapter(val cards: List<NestedData.Card>) : RecyclerView.Adapter<NestedCardAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_demo_nested_card, parent, false)
        return VH(view)
    }
    override fun getItemCount(): Int = cards.size
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(cards[position])

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val card: FrameLayout = view.findViewById(R.id.nested_card)
        private val title: TextView = view.findViewById(R.id.nested_card_title)
        private val subtitle: TextView = view.findViewById(R.id.nested_card_subtitle)
        private val tag: TextView = view.findViewById(R.id.nested_card_tag)
        fun bind(c: NestedData.Card) {
            title.text = c.title
            subtitle.text = c.subtitle
            tag.text = c.tag
            val d = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(c.start, c.end))
            d.cornerRadius = 16f * card.resources.displayMetrics.density
            card.background = d
            card.setOnClickListener {
                card.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).withEndAction {
                    card.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }.start()
            }
        }
    }
}

object NestedData {
    data class Card(val title: String, val subtitle: String, val tag: String, val start: Int, val end: Int)
    data class Section(val title: String, val subtitle: String, val cards: List<Card>)

    fun sections(): List<Section> = listOf(
        Section("热门推荐", "本周排行", listOf(
            Card("Compose", "声明式 UI", "热", 0xFF6A8DFF.toInt(), 0xFF8E66FF.toInt()),
            Card("协程", "Flow / Channel", "进阶", 0xFFFF8A65.toInt(), 0xFFFF5E8C.toInt()),
            Card("动画", "MotionLayout", "动效", 0xFF22C1C3.toInt(), 0xFF3CCB8A.toInt()),
            Card("性能", "启动 / 渲染", "实战", 0xFFFFB94B.toInt(), 0xFFFF7E5F.toInt()),
            Card("Material3", "动态色", "设计", 0xFFA18CD1.toInt(), 0xFFFBC2EB.toInt()),
        )),
        Section("最新课程", "刚刚上架", listOf(
            Card("ViewBinding", "替代 findViewById", "新", 0xFF36D1DC.toInt(), 0xFF5B86E5.toInt()),
            Card("Room", "数据库实战", "新", 0xFFFF9966.toInt(), 0xFFFF5E62.toInt()),
            Card("WorkManager", "后台任务", "新", 0xFFF7971E.toInt(), 0xFFFFD200.toInt()),
            Card("DataStore", "替代 SP", "新", 0xFF11998E.toInt(), 0xFF38EF7D.toInt()),
        )),
        Section("精选合集", "进阶必看", listOf(
            Card("RecyclerView", "深入源码", "推荐", 0xFFE65C00.toInt(), 0xFFF9D423.toInt()),
            Card("自定义 View", "绘制 / 事件", "推荐", 0xFF8E2DE2.toInt(), 0xFF4A00E0.toInt()),
            Card("WindowInsets", "全屏适配", "推荐", 0xFF1FA2FF.toInt(), 0xFF12D8FA.toInt()),
            Card("可访问性", "无障碍", "推荐", 0xFFF953C6.toInt(), 0xFFB91D73.toInt()),
        )),
        Section("学习路径", "从入门到精通", listOf(
            Card("基础", "四大组件", "Lv1", 0xFF56AB2F.toInt(), 0xFFA8E063.toInt()),
            Card("进阶", "架构与组件化", "Lv2", 0xFF5A3F37.toInt(), 0xFF2C7744.toInt()),
            Card("高阶", "性能与稳定性", "Lv3", 0xFF373B44.toInt(), 0xFF4286F4.toInt()),
        )),
    )
}
