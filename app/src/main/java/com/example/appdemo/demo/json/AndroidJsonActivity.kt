package com.example.appdemo.demo.json

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
 * JSON / 字符串处理总入口。
 *
 *  1. 字符串常用操作：拼接、格式化、分割、替换、正则、编码
 *  2. JSONObject / JSONArray：Android 内置手写解析
 *  3. Gson 序列化/反序列化：数据类 + TypeToken + @SerializedName
 *  4. 复杂嵌套 JSON：多层对象 + 数组混合
 *  5. JsonReader 流式解析：低内存边读边解析
 *  6. 从 assets 读取 JSON 文件
 */
class AndroidJsonActivity : AppCompatActivity() {

    private data class NavItem(val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_json)
        setupDemoToolbar(R.string.json_title, R.id.android_json_root)

        val entries = listOf(
            NavItem(getString(R.string.json_string_ops), getString(R.string.json_string_ops_hint), StringOpsDemoActivity::class.java),
            NavItem(getString(R.string.json_object), getString(R.string.json_object_hint), JsonObjectDemoActivity::class.java),
            NavItem(getString(R.string.json_gson), getString(R.string.json_gson_hint), GsonDemoActivity::class.java),
            NavItem(getString(R.string.json_nested), getString(R.string.json_nested_hint), NestedJsonDemoActivity::class.java),
            NavItem(getString(R.string.json_stream), getString(R.string.json_stream_hint), JsonStreamDemoActivity::class.java),
            NavItem(getString(R.string.json_assets), getString(R.string.json_assets_hint), JsonAssetsDemoActivity::class.java),
        )

        val list = findViewById<RecyclerView>(R.id.android_json_list)
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
