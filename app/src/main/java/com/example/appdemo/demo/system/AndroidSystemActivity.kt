package com.example.appdemo.demo.system

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
 * 系统能力总入口 —— 列出 5 个平台能力 Demo。
 *
 *   1. 权限动态申请：ActivityResult API
 *   2. 通知：NotificationChannel（Android 8+ 必须）
 *   3. Service：前台 Service + Binder
 *   4. 广播：BroadcastReceiver 静态/动态注册
 *   5. Handler + Looper + HandlerThread：线程通信
 */
class AndroidSystemActivity : AppCompatActivity() {

    private data class NavItem(val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_system)
        setupDemoToolbar(R.string.system_title, R.id.android_system_root)

        val entries = listOf(
            NavItem(getString(R.string.system_permission), getString(R.string.system_permission_hint), PermissionDemoActivity::class.java),
            NavItem(getString(R.string.system_notification), getString(R.string.system_notification_hint), NotificationDemoActivity::class.java),
            NavItem(getString(R.string.system_service), getString(R.string.system_service_hint), ServiceDemoActivity::class.java),
            NavItem(getString(R.string.system_broadcast), getString(R.string.system_broadcast_hint), BroadcastDemoActivity::class.java),
            NavItem(getString(R.string.system_handler), getString(R.string.system_handler_hint), HandlerDemoActivity::class.java),
        )

        val list = findViewById<RecyclerView>(R.id.android_system_list)
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
