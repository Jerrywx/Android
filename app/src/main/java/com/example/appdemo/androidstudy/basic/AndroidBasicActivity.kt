package com.example.appdemo.androidstudy.basic

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

class AndroidBasicActivity : AppCompatActivity() {

    /// 创建数据类
    private data class ItemModel(
        val title: String,
        val content: String,
        val target: Class<*>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_basic)
        setupDemoToolbar(R.string.android_basic_title, R.id.android_basic_root)

        ///
        val entries = listOf<ItemModel>(
            ItemModel("Android 基础", "Android 基础", AndroidBasicActivity::class.java),
            ItemModel("Android 基础", "Android 基础", AndroidBasicActivity::class.java),
            ItemModel("Android 基础", "Android 基础", AndroidBasicActivity::class.java),
            ItemModel("Android 基础", "Android 基础", AndroidBasicActivity::class.java),
            ItemModel("Android 基础", "Android 基础", AndroidBasicActivity::class.java),
            ItemModel("Android 基础", "Android 基础", AndroidBasicActivity::class.java),
        )

        /// 创建 RecyclerView
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.android_basic_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = EntryAdapter(entries, {
            startActivity(Intent(this, it.target))
        })

    }

    /// 创建 Adapter, 为什么两个参数 entries 和 listener 都是 private 的？
    /// 1. 为了封装数据，避免外部直接修改数据
    /// 2) 为了封装事件，避免外部直接调用事件
    private class EntryAdapter (
        private val entries: List<ItemModel>,
        private val listener: (ItemModel) -> Unit,
    ) : RecyclerView.Adapter<EntryAdapter.VH>() {

        /// 创建 ViewHolder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_basic_entry, parent, false))
        }
        /// 绑定数据
        override fun onBindViewHolder(holder: EntryAdapter.VH, position: Int) {
            holder.bind(entries[position], position + 1, listener)
        }

        override fun getItemCount(): Int {
            return entries.size
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
//            private val card: View = view.findViewById(R.id.study_card)
//            private val index: TextView = view.findViewById(R.id.study_index)
//            private val title: TextView = view.findViewById(R.id.study_title)
//            private val subtitle: TextView = view.findViewById(R.id.study_subtitle)
//            fun bind(item: ItemModel, no: Int, onClick: (ItemModel) -> Unit) {
//                index.text = no.toString()
//                title.text = item.title
//                subtitle.text = item.content
//                card.setOnClickListener { onClick(item) }
//            }
            private val card: View = view.findViewById(R.id.basic_card)
            private val index: TextView = view.findViewById(R.id.basic_index)
            private val title: TextView = view.findViewById(R.id.basic_title)
            private val subtitle: TextView = view.findViewById(R.id.basic_subtitle)
            fun bind(item: ItemModel, no: Int, onClick: (ItemModel) -> Unit) {
                index.text = no.toString()
                title.text = item.title
                subtitle.text = item.content
                card.setOnClickListener { onClick(item) }
            }
        }
    }
}
