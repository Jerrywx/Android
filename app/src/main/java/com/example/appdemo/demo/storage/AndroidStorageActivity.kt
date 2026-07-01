package com.example.appdemo.demo.storage

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
 * 数据存储总入口 —— 按类型列出全部持久化 Demo。
 *
 *   1. SharedPreferences：轻量键值对
 *   2. DataStore（Preferences）：协程 + Flow 版键值对
 *   3. SQLite 原生：SQLiteOpenHelper CRUD
 *   4. 文件读写：内部 / 外部 / cache 目录
 */
class AndroidStorageActivity : AppCompatActivity() {

    private data class NavItem(val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_storage)
        setupDemoToolbar(R.string.storage_title, R.id.android_storage_root)

        val entries = listOf(
            NavItem(getString(R.string.storage_sp), getString(R.string.storage_sp_hint), SpDemoActivity::class.java),
            NavItem(getString(R.string.storage_datastore), getString(R.string.storage_datastore_hint), DataStoreDemoActivity::class.java),
            NavItem(getString(R.string.storage_sqlite), getString(R.string.storage_sqlite_hint), SqliteDemoActivity::class.java),
            NavItem(getString(R.string.storage_file), getString(R.string.storage_file_hint), FileIoDemoActivity::class.java),
        )

        val list = findViewById<RecyclerView>(R.id.android_storage_list)
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
