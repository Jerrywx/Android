package com.example.appdemo.demo.customview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.customview.widget.PullToRefreshLayout
import com.example.appdemo.demo.customview.widget.RefreshHeaderView

/**
 * 下拉刷新 —— 综合练手 Demo。
 *
 * 涵盖：
 *   1) 自定义 ViewGroup 的事件拦截与冲突处理
 *   2) 阻尼下拉 + 越过阈值触发刷新
 *   3) 状态机（IDLE / PULL / RELEASE_TO_REFRESH / REFRESHING）
 *   4) Header 根据状态切换文案 / 图标旋转
 */
class PullToRefreshDemoActivity : AppCompatActivity() {

    private val items = mutableListOf<String>()
    private lateinit var adapter: SimpleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customview_pull_refresh)
        setupDemoToolbar(R.string.customview_refresh_title, R.id.customview_pull_root)

        repeat(15) { items.add("列表项 ${it + 1}") }
        adapter = SimpleAdapter(items)

        val list = findViewById<RecyclerView>(R.id.customview_pull_list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        val refresh = findViewById<PullToRefreshLayout>(R.id.customview_pull_refresh)
        val header = findViewById<RefreshHeaderView>(R.id.customview_pull_header)
        refresh.setOnRefreshListener(object : PullToRefreshLayout.OnRefreshListener {
            override fun onStateChanged(
                state: PullToRefreshLayout.State,
                offset: Int,
                triggerDistance: Int,
            ) {
                header.applyState(state, offset, triggerDistance)
            }

            override fun onRefresh() {
                /// 模拟网络请求 1.2s 后完成
                refresh.postDelayed({
                    items.add(0, "新增于 ${System.currentTimeMillis() / 1000}")
                    adapter.notifyItemInserted(0)
                    list.scrollToPosition(0)
                    refresh.finishRefresh()
                }, 1200)
            }
        })
    }

    private class SimpleAdapter(private val data: List<String>) : RecyclerView.Adapter<SimpleAdapter.VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val tv = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
            tv.setPadding(48, 32, 48, 32)
            return VH(tv)
        }
        override fun getItemCount(): Int = data.size
        override fun onBindViewHolder(holder: VH, position: Int) {
            (holder.itemView as TextView).text = data[position]
        }
        class VH(view: View) : RecyclerView.ViewHolder(view)
    }
}
