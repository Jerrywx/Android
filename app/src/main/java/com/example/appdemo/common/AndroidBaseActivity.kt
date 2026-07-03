package com.example.appdemo.common

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
import com.example.appdemo.demo.study.AndroidStudyActivity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AndroidBaseActivity : AppCompatActivity() {

    private data class NavItem(val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_base)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.android_base_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        /// 点击返回按钮
        findViewById<ImageButton>(R.id.btn_android_base_back).setOnClickListener { finish() }

        val entries = listOf(
            NavItem(
                getString(R.string.android_base_recycler_view),
                getString(R.string.android_base_recycler_view_hint),
                AndroidRecyclerViewActivity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_view_pager2),
                getString(R.string.android_base_view_pager2_hint),
                AndroidViewPager2Activity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_layout),
                getString(R.string.android_base_layout_hint),
                AndroidLayoutActivity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_coroutine),
                getString(R.string.android_base_coroutine_hint),
                CoroutineDemoActivity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_network),
                getString(R.string.android_base_network_hint),
                AndroidNetworkActivity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_animation),
                getString(R.string.android_base_animation_hint),
                AndroidAnimationActivity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_fragment),
                getString(R.string.android_base_fragment_hint),
                AndroidFragmentActivity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_storage),
                getString(R.string.android_base_storage_hint),
                com.example.appdemo.demo.storage.AndroidStorageActivity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_jetpack),
                getString(R.string.android_base_jetpack_hint),
                com.example.appdemo.demo.jetpack.AndroidJetpackActivity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_system),
                getString(R.string.android_base_system_hint),
                com.example.appdemo.demo.system.AndroidSystemActivity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_graphics),
                getString(R.string.android_base_graphics_hint),
                com.example.appdemo.demo.graphics.AndroidGraphicsActivity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_customview),
                getString(R.string.android_base_customview_hint),
                com.example.appdemo.demo.customview.AndroidCustomViewActivity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_media),
                getString(R.string.android_base_media_hint),
                com.example.appdemo.demo.media.AndroidMediaActivity::class.java,
            ),
            NavItem(
                getString(R.string.android_base_floating_ball),
                getString(R.string.android_base_floating_ball_hint),
                AndroidStudyActivity::class.java,
            ),
        )
        /// 获取列表
        val list = findViewById<RecyclerView>(R.id.android_base_list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = EntryAdapter(entries) { entry ->
            startActivity(Intent(this, entry.target))
        }
    }

    private class EntryAdapter(
        private val items: List<NavItem>,
        private val onClick: (NavItem) -> Unit,
    ) : RecyclerView.Adapter<EntryAdapter.VH>() {

        /// 主要作用是创建并返回一个新的 ViewHolder 实例
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            /// 创建 ViewHolder 实例
            /// 1. 为了封装数据，避免外部直接修改数据
            /// 2) 为了封装事件，避免外部直接调用事件
            /// LayoutInflater 是干什么用的？
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_study_entry, parent, false)
            /// 返回 ViewHolder 实例
            return VH(view)
        }
        /// 获取列表项的个数
        override fun getItemCount(): Int = items.size
        /// 绑定数据
        override fun onBindViewHolder(holder: VH, position: Int) =
            holder.bind(items[position], position + 1, onClick)

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
