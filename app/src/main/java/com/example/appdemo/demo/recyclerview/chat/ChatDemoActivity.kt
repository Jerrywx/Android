package com.example.appdemo.demo.recyclerview.chat

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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatDemoActivity : AppCompatActivity() {

    private val messages = mutableListOf(
        ChatItem("Bot", "你好，我是机器人小助手。", false),
        ChatItem("我", "RecyclerView 怎么做聊天列表？", true),
        ChatItem("Bot", "把 LayoutManager 设成 stackFromEnd = true，新消息添加后调用 scrollToPosition 就好。", false),
        ChatItem("我", "气泡两边对齐怎么做？", true),
        ChatItem("Bot", "用两种 ViewType + gravity start/end，再配两个气泡 drawable。", false),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo_chat)
        setupDemoToolbar(R.string.chat_demo_title)

        val list = findViewById<RecyclerView>(R.id.chat_demo_list)
        val lm = LinearLayoutManager(this)
        lm.stackFromEnd = true
        list.layoutManager = lm
        val adapter = ChatDemoAdapter(messages)
        list.adapter = adapter
        list.itemAnimator = ChatItemAnimator()
        list.scrollToPosition(messages.size - 1)

        val input = findViewById<EditText>(R.id.chat_demo_input)
        findViewById<Button>(R.id.chat_demo_send).setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            messages.add(ChatItem("我", text, true))
            adapter.notifyItemInserted(messages.size - 1)
            list.smoothScrollToPosition(messages.size - 1)
            input.setText("")
            input.postDelayed({
                messages.add(ChatItem("Bot", "收到：$text", false))
                adapter.notifyItemInserted(messages.size - 1)
                list.smoothScrollToPosition(messages.size - 1)
            }, 700)
        }
    }
}

data class ChatItem(val who: String, val text: String, val fromMe: Boolean)

private class ChatDemoAdapter(val items: List<ChatItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemViewType(position: Int): Int = if (items[position].fromMe) 1 else 0
    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutId = if (viewType == 1) R.layout.item_demo_chat_sent else R.layout.item_demo_chat_received
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as VH).bind(items[position])
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val text: TextView = view.findViewById(R.id.chat_demo_text)
        fun bind(item: ChatItem) { text.text = item.text }
    }
}

private class ChatItemAnimator : androidx.recyclerview.widget.DefaultItemAnimator() {
    init { addDuration = 220 }
    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.translationY = 30f * holder.itemView.resources.displayMetrics.density
        holder.itemView.alpha = 0f
        holder.itemView.animate().translationY(0f).alpha(1f)
            .setDuration(addDuration)
            .withEndAction { dispatchAddFinished(holder) }
            .start()
        return true
    }
}
