package com.example.appdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ConcatAdapterDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo_concat)
        setupDemoToolbar(R.string.concat_title)

        val list = findViewById<RecyclerView>(R.id.concat_list)
        list.layoutManager = LinearLayoutManager(this)

        val header = HeaderAdapter()
        val sectionA = SectionTitleAdapter("最近任务")
        val cardsA = CardAdapter(ConcatData.tasks())
        val sectionB = SectionTitleAdapter("学习清单")
        val cardsB = CardAdapter(ConcatData.lessons())
        val sectionC = SectionTitleAdapter("常用工具")
        val cardsC = CardAdapter(ConcatData.tools())
        val footer = FooterAdapter()

        list.adapter = ConcatAdapter(
            header, sectionA, cardsA, sectionB, cardsB, sectionC, cardsC, footer
        )
    }
}

private class HeaderAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_demo_concat_header, parent, false)
        return object : RecyclerView.ViewHolder(v) {}
    }
    override fun getItemCount() = 1
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = Unit
}

private class FooterAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val tv = TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = android.view.Gravity.CENTER
            setTextColor(context.getColor(R.color.demo_hint))
            textSize = 12f
            setPadding(0, (20 * resources.displayMetrics.density).toInt(), 0, (20 * resources.displayMetrics.density).toInt())
            text = "— 到底啦 —"
        }
        return object : RecyclerView.ViewHolder(tv) {}
    }
    override fun getItemCount() = 1
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = Unit
}

private class SectionTitleAdapter(private val title: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_demo_concat_section, parent, false) as TextView
        v.text = title
        return object : RecyclerView.ViewHolder(v) {}
    }
    override fun getItemCount() = 1
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder.itemView as TextView).text = title
    }
}

private class CardAdapter(val items: List<ConcatData.Card>) : RecyclerView.Adapter<CardAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_demo_concat_card, parent, false)
        return VH(v)
    }
    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val card: View = view.findViewById(R.id.concat_card)
        private val dot: View = view.findViewById(R.id.concat_dot)
        private val title: TextView = view.findViewById(R.id.concat_title)
        private val subtitle: TextView = view.findViewById(R.id.concat_subtitle)
        fun bind(c: ConcatData.Card) {
            title.text = c.title
            subtitle.text = c.subtitle
            dot.backgroundTintList = android.content.res.ColorStateList.valueOf(c.color)
            card.setOnClickListener {
                card.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction {
                    card.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }.start()
            }
        }
    }
}

object ConcatData {
    data class Card(val title: String, val subtitle: String, val color: Int)
    fun tasks(): List<Card> = listOf(
        Card("代码评审", "下午 3:00", 0xFF6A8DFF.toInt()),
        Card("接口联调", "下午 4:00", 0xFFFF8A65.toInt()),
        Card("性能调试", "晚上 8:00", 0xFF22C1C3.toInt()),
    )
    fun lessons(): List<Card> = listOf(
        Card("Compose 入门", "12 课时", 0xFF8E66FF.toInt()),
        Card("协程进阶", "8 课时", 0xFFFF5E8C.toInt()),
        Card("RecyclerView 深度", "16 课时", 0xFF3CCB8A.toInt()),
        Card("自定义 View", "10 课时", 0xFFFFB94B.toInt()),
    )
    fun tools(): List<Card> = listOf(
        Card("Layout Inspector", "查看视图层级", 0xFF5B86E5.toInt()),
        Card("Profiler", "性能分析", 0xFFFF6B6B.toInt()),
        Card("LeakCanary", "内存泄漏", 0xFF11998E.toInt()),
    )
}
