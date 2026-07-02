package com.example.appdemo.demo.jetpack

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
 * Jetpack 总入口 —— 按主题列出五个核心组件 Demo。
 *
 *   1. ViewModel + LiveData：数据保活与观察
 *   2. Flow / StateFlow / SharedFlow：冷流与热流对比
 *   3. Navigation Component：单 Activity + 多 Fragment
 *   4. WorkManager：可靠后台任务
 *   5. Lifecycle：生命周期感知
 */
class AndroidJetpackActivity : AppCompatActivity() {

    private data class NavItem(val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_jetpack)
        setupDemoToolbar(R.string.jetpack_title, R.id.android_jetpack_root)

        val entries = listOf(
            NavItem(getString(R.string.jetpack_vm), getString(R.string.jetpack_vm_hint), ViewModelLiveDataDemoActivity::class.java),
            NavItem(getString(R.string.jetpack_flow), getString(R.string.jetpack_flow_hint), FlowDemoActivity::class.java),
            NavItem(getString(R.string.jetpack_nav), getString(R.string.jetpack_nav_hint), NavigationDemoActivity::class.java),
            NavItem(getString(R.string.jetpack_work), getString(R.string.jetpack_work_hint), WorkManagerDemoActivity::class.java),
            NavItem(getString(R.string.jetpack_lifecycle), getString(R.string.jetpack_lifecycle_hint), LifecycleDemoActivity::class.java),
        )

        val list = findViewById<RecyclerView>(R.id.android_jetpack_list)
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
