package com.example.appdemo.demo.customview

import android.content.Intent
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

/**
 * 自定义 View 总入口 —— 按难度递进列出 Demo。
 *
 *   1. 圆形进度条：Canvas 基础绘制 + 属性动画
 *   （后续会继续追加：流式标签布局、手写签名板、缩放 ImageView、下拉刷新…）
 */
class AndroidCustomViewActivity : AppCompatActivity() {

    private data class NavItem(val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_customview)
        setupDemoToolbar(R.string.customview_title, R.id.android_customview_root)

        val entries = listOf(
            NavItem(
                getString(R.string.customview_circle_progress),
                getString(R.string.customview_circle_progress_hint),
                CircleProgressDemoActivity::class.java,
            ),
            NavItem(
                getString(R.string.customview_flow),
                getString(R.string.customview_flow_hint),
                FlowLayoutDemoActivity::class.java,
            ),
            NavItem(
                getString(R.string.customview_drag_ball),
                getString(R.string.customview_drag_ball_hint),
                FloatingBallDemoActivity::class.java,
            ),
            NavItem(
                getString(R.string.customview_refresh),
                getString(R.string.customview_refresh_hint),
                PullToRefreshDemoActivity::class.java,
            ),
            NavItem(
                getString(R.string.customview_clear_edit),
                getString(R.string.customview_clear_edit_hint),
                ClearableEditTextDemoActivity::class.java,
            ),
            NavItem(
                getString(R.string.customview_ripple),
                getString(R.string.customview_ripple_hint),
                RippleClickDemoActivity::class.java,
            ),
            NavItem(
                getString(R.string.customview_rating),
                getString(R.string.customview_rating_hint),
                StarRatingDemoActivity::class.java,
            ),
            NavItem(
                getString(R.string.customview_wave),
                getString(R.string.customview_wave_hint),
                WaveDemoActivity::class.java,
            ),
            NavItem(
                getString(R.string.customview_sticky),
                getString(R.string.customview_sticky_hint),
                StickyDropDemoActivity::class.java,
            ),
            NavItem(
                getString(R.string.customview_dashboard),
                getString(R.string.customview_dashboard_hint),
                DashboardDemoActivity::class.java,
            ),
            NavItem(
                getString(R.string.customview_ruler),
                getString(R.string.customview_ruler_hint),
                RulerDemoActivity::class.java,
            ),
        )

        val list = findViewById<RecyclerView>(R.id.android_customview_list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = EntryAdapter(entries) { entry -> startActivity(Intent(this, entry.target)) }
    }

    private class EntryAdapter(
        private val items: List<NavItem>,
        private val onClick: (NavItem) -> Unit,
    ) : RecyclerView.Adapter<EntryAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_study_entry, parent, false)
            return VH(view)
        }
        override fun getItemCount(): Int = items.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position], position + 1, onClick)

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            private val card: View = view.findViewById(R.id.study_card)
            private val index: TextView = view.findViewById(R.id.study_index)
            private val title: TextView = view.findViewById(R.id.study_title)
            private val subtitle: TextView = view.findViewById(R.id.study_subtitle)
            fun bind(item: NavItem, no: Int, onClick: (NavItem) -> Unit) {
                index.text = no.toString()
                title.text = item.title
                subtitle.text = item.subtitle
                card.setOnClickListener { onClick(item) }
            }
        }
    }
}
