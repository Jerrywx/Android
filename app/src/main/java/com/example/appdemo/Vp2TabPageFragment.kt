package com.example.appdemo

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class Vp2TabPageFragment : Fragment(R.layout.fragment_vp2_tab_page) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getString(ARG_TITLE).orEmpty()
        val seed = arguments?.getInt(ARG_SEED) ?: 0
        val swipe = view.findViewById<SwipeRefreshLayout>(R.id.vp2_tab_swipe)
        val list = view.findViewById<RecyclerView>(R.id.vp2_tab_list)

        val data = Vp2TabData.build(title, seed)
        val adapter = TabAdapter(data)
        list.layoutManager = LinearLayoutManager(view.context)
        list.adapter = adapter
        swipe.setColorSchemeColors(0xFF07C160.toInt())
        swipe.setOnRefreshListener {
            list.postDelayed({
                adapter.shuffle()
                swipe.isRefreshing = false
            }, 700)
        }
    }

    private class TabAdapter(private var items: List<Vp2TabData.Card>) : RecyclerView.Adapter<TabAdapter.VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vp2_tab_content, parent, false)
            return VH(v)
        }
        override fun getItemCount(): Int = items.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

        fun shuffle() {
            items = items.shuffled()
            notifyDataSetChanged()
        }

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            private val cover: FrameLayout = v.findViewById(R.id.vp2_tab_cover)
            private val tag: TextView = v.findViewById(R.id.vp2_tab_tag)
            private val title: TextView = v.findViewById(R.id.vp2_tab_title)
            private val sub: TextView = v.findViewById(R.id.vp2_tab_sub)
            private val meta: TextView = v.findViewById(R.id.vp2_tab_meta)
            fun bind(c: Vp2TabData.Card) {
                tag.text = c.tag
                title.text = c.title
                sub.text = c.sub
                meta.text = c.meta
                val d = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(c.start, c.end))
                d.cornerRadius = 14f * cover.resources.displayMetrics.density
                cover.background = d
            }
        }
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_SEED = "seed"
        fun newInstance(title: String, seed: Int): Vp2TabPageFragment {
            val f = Vp2TabPageFragment()
            f.arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putInt(ARG_SEED, seed)
            }
            return f
        }
    }
}

object Vp2TabData {
    data class Card(val tag: String, val title: String, val sub: String, val meta: String, val start: Int, val end: Int)

    private val palette = listOf(
        0xFF6A8DFF.toInt() to 0xFF8E66FF.toInt(),
        0xFFFF8A65.toInt() to 0xFFFF5E8C.toInt(),
        0xFF22C1C3.toInt() to 0xFF3CCB8A.toInt(),
        0xFFFFB94B.toInt() to 0xFFFF7E5F.toInt(),
        0xFFA18CD1.toInt() to 0xFFFBC2EB.toInt(),
        0xFF4FACFE.toInt() to 0xFF00F2FE.toInt(),
    )

    fun build(tag: String, seed: Int): List<Card> {
        val titles = listOf(
            "Kotlin 协程实战", "Compose 动效进阶", "Material 3 适配",
            "Jetpack 全家桶", "性能优化路线", "架构演进案例",
            "动画那些事", "WorkManager 解析", "FlowState 全景",
            "Navigation 进阶", "MVI 实战", "测试驱动开发",
        )
        val subs = listOf(
            "从 0 到 1 的完整路径", "经验沉淀 · 一文掌握", "覆盖常见踩坑点",
            "工程师必读 · 收藏夹必备", "项目落地分享", "实战代码完整开放",
        )
        return List(12) { i ->
            val (s, e) = palette[(i + seed) % palette.size]
            Card(
                tag = tag,
                title = titles[(i + seed) % titles.size],
                sub = subs[(i + seed * 2) % subs.size],
                meta = "${(120 + i * 37) % 999 + 100} 阅读 · ${(i + 3) * 4 + seed} 分钟前",
                start = s,
                end = e,
            )
        }
    }
}
