package com.example.appdemo.demo.graphics

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
 * 图形绘制总入口 —— 列出 6 个图形 Demo。
 *
 *   1. Canvas 基础：时钟 + 折线图
 *   2. Path 绘制：手写签名板
 *   3. PorterDuff：刮刮卡 + 圆头像
 *   4. Shader：渐变着色
 *   5. 手势缩放平移：双指 ImageView
 *   6. SurfaceView：粒子动画
 */
class AndroidGraphicsActivity : AppCompatActivity() {

    private data class NavItem(val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_graphics)
        setupDemoToolbar(R.string.graphics_title, R.id.android_graphics_root)

        val entries = listOf(
            NavItem(getString(R.string.graphics_canvas), getString(R.string.graphics_canvas_hint), CanvasBasicDemoActivity::class.java),
            NavItem(getString(R.string.graphics_path), getString(R.string.graphics_path_hint), PathDemoActivity::class.java),
            NavItem(getString(R.string.graphics_porterduff), getString(R.string.graphics_porterduff_hint), PorterDuffDemoActivity::class.java),
            NavItem(getString(R.string.graphics_shader), getString(R.string.graphics_shader_hint), ShaderDemoActivity::class.java),
            NavItem(getString(R.string.graphics_zoom), getString(R.string.graphics_zoom_hint), ZoomImageDemoActivity::class.java),
            NavItem(getString(R.string.graphics_surface), getString(R.string.graphics_surface_hint), SurfaceViewDemoActivity::class.java),
        )

        val list = findViewById<RecyclerView>(R.id.android_graphics_list)
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
