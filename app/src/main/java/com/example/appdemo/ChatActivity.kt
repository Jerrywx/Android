package com.example.appdemo

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            insets
        }

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        findViewById<TextView>(R.id.chat_title).text = title

        val messages = FakeChats.forTitle(title).toMutableList()

        val list = findViewById<RecyclerView>(R.id.chat_list)
        val adapter = ChatAdapter(messages, title.firstOrNull()?.toString().orEmpty())
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val input = findViewById<EditText>(R.id.chat_input)
        val send = findViewById<View>(R.id.btn_send)
        send.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            messages.add(ChatMessage(text = text, fromMe = true, time = "刚刚"))
            adapter.notifyItemInserted(messages.size - 1)
            list.scrollToPosition(messages.size - 1)
            input.setText("")
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
    }
}
