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

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SearchFilterDemoActivity : AppCompatActivity() {

    private val all = SearchData.all()
    private lateinit var adapter: SearchAdapter
    private lateinit var empty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo_search)
        setupDemoToolbar(R.string.search_filter_title)

        val list = findViewById<RecyclerView>(R.id.search_list)
        list.layoutManager = LinearLayoutManager(this)
        adapter = SearchAdapter()
        list.adapter = adapter
        list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
            addDuration = 220; removeDuration = 160; moveDuration = 220
        }
        empty = findViewById(R.id.search_empty)

        adapter.submit(all.map { SearchAdapter.Row(it, "") })

        findViewById<EditText>(R.id.search_input).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) = applyFilter(s?.toString().orEmpty())
        })
    }

    private fun applyFilter(query: String) {
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) all
        else all.filter { it.title.lowercase().contains(q) || it.subtitle.lowercase().contains(q) }
        adapter.submit(filtered.map { SearchAdapter.Row(it, q) })
        empty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}

private class SearchAdapter : ListAdapter<SearchAdapter.Row, SearchAdapter.VH>(DIFF) {
    data class Row(val item: SearchData.Item, val query: String)

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Row>() {
            override fun areItemsTheSame(o: Row, n: Row) = o.item.id == n.item.id
            override fun areContentsTheSame(o: Row, n: Row) = o == n
        }
    }

    fun submit(rows: List<Row>) = submitList(rows)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_demo_search, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val avatar: TextView = view.findViewById(R.id.search_avatar)
        private val title: TextView = view.findViewById(R.id.search_title)
        private val subtitle: TextView = view.findViewById(R.id.search_subtitle)

        fun bind(row: Row) {
            val item = row.item
            avatar.text = item.title.firstOrNull()?.toString().orEmpty()
            val d = GradientDrawable()
            d.shape = GradientDrawable.OVAL
            d.setColor(item.color)
            avatar.background = d
            title.text = highlight(item.title, row.query, title.context)
            subtitle.text = highlight(item.subtitle, row.query, subtitle.context)
        }

        private fun highlight(text: String, query: String, ctx: android.content.Context): CharSequence {
            if (query.isEmpty()) return text
            val src = text
            val lower = src.lowercase()
            val sp = SpannableString(src)
            var start = 0
            while (true) {
                val idx = lower.indexOf(query, start)
                if (idx < 0) break
                sp.setSpan(BackgroundColorSpan(ctx.getColor(R.color.demo_search_highlight)),
                    idx, idx + query.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                sp.setSpan(ForegroundColorSpan(Color.parseColor("#FF1F2933")),
                    idx, idx + query.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                start = idx + query.length
            }
            return sp
        }
    }
}

object SearchData {
    data class Item(val id: Long, val title: String, val subtitle: String, val color: Int)

    fun all(): List<Item> {
        val rows = listOf(
            "Kotlin 协程入门" to "理解结构化并发与作用域",
            "Flow 与 StateFlow" to "替代 LiveData 的响应式数据流",
            "Channel 与 Buffer" to "理解协程通信原语",
            "Compose 状态管理" to "remember / derivedStateOf / produceState",
            "Compose 性能调优" to "避免不必要的重组",
            "RecyclerView 基础" to "Adapter / LayoutManager / ViewHolder",
            "RecyclerView 进阶" to "ItemTouchHelper / SnapHelper / 多类型",
            "DiffUtil 与 ListAdapter" to "差分计算与局部刷新",
            "Paging 3" to "分页加载与缓存",
            "WorkManager" to "可靠的后台任务调度",
            "Hilt 依赖注入" to "Android 推荐的 DI 框架",
            "Room 数据库" to "SQL + 类型安全访问",
            "DataStore" to "替代 SharedPreferences",
            "MotionLayout" to "复杂过渡动画",
            "Material 3" to "动态色彩与组件",
            "Navigation 组件" to "导航图与回退栈",
            "WindowInsets 进阶" to "Edge-to-Edge 适配",
            "可访问性" to "TalkBack 与最小点击区域",
            "性能优化总览" to "启动 / 渲染 / 内存 / 稳定性",
            "线上事故复盘" to "从崩溃到根因分析",
        )
        val palette = intArrayOf(
            0xFF6A8DFF.toInt(), 0xFFFF8A65.toInt(), 0xFF22C1C3.toInt(),
            0xFFFFB94B.toInt(), 0xFF8E66FF.toInt(), 0xFFFF5E8C.toInt(),
            0xFF3CCB8A.toInt(), 0xFFFF7E5F.toInt(),
        )
        return rows.mapIndexed { i, (t, s) -> Item(i.toLong(), t, s, palette[i % palette.size]) }
    }
}
