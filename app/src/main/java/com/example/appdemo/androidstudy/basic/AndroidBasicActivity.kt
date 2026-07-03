package com.example.appdemo.androidstudy.basic

import android.content.ClipDescription
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appdemo.R
import com.bumptech.glide.Glide
import com.example.appdemo.common.setupDemoToolbar
import com.google.gson.Gson
import kotlin.random.Random

class AndroidBasicActivity : AppCompatActivity() {

    /// 创建数据类
    private data class ItemModel(
        val title: String,
        val content: String,
        val description: String,
        val bookCover: String,
        val target: Class<*>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_basic)
        setupDemoToolbar(R.string.android_basic_title, R.id.android_basic_root)

        /// 从 assets/books.json 中加载并解析为 List<BookModel>
        val books = loadBooks()

        /// 将 BookModel 映射为列表使用的 ItemModel
        val entries = books.map { book ->
            ItemModel(
                title = book.name,
                content = "${book.authorName} · ${book.categoryName} · ${book.chapterCount}章",
                description = book.description,
                bookCover = book.picUrl,
                target = AndroidBasicActivity::class.java
            )
        }

        /// 创建 RecyclerView
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.android_basic_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = EntryAdapter(entries, {
            startActivity(Intent(this, it.target))
        })

    }

    /// 从 assets/books.json 读取并解析为 List<BookModel>
    /// 参考 JsonAssetsDemoActivity 的读取风格
    private fun loadBooks(): List<BookModel> {
        val text = assets.open("books.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val response = Gson().fromJson(text, BookListResponse::class.java)
        /// 外层 result 是 List<BookItem>，每个 BookItem 里的 result 才是 BookModel
        return response.result.mapNotNull { it.result }
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
            private val bookName: TextView = view.findViewById(R.id.book_name)
            private val bookdesc: TextView = view.findViewById(R.id.book_description)
            private val bookCover: ImageView = view.findViewById(R.id.book_cover)
            private val bookAuthor: TextView = view.findViewById(R.id.boot_author)

            /// 给 card 设置随机色
            private fun setRandomColor() {
                card.setBackgroundColor(
                    Color.rgb(
                        Random.nextInt(256),
                        Random.nextInt(256),
                        Random.nextInt(256),
                    )
                )
            }

            fun bind(item: ItemModel, no: Int, onClick: (ItemModel) -> Unit) {
                bookName.text = item.title
                bookdesc.text = item.description
                bookAuthor.text = item.content

                /// 使用 Glide 加载封面图
                Glide.with(bookCover)
                    .load(item.bookCover)
                    .centerCrop()
                    .into(bookCover)

                card.setOnClickListener { onClick(item) }
//                setRandomColor()
            }
        }
    }
}
