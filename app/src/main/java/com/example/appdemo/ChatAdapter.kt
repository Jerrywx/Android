package com.example.appdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val items: List<ChatMessage>,
    private val peerAvatarLetter: String,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int) =
        if (items[position].fromMe) TYPE_SENT else TYPE_RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SENT) {
            SentVH(inflater.inflate(R.layout.item_chat_sent, parent, false))
        } else {
            ReceivedVH(inflater.inflate(R.layout.item_chat_received, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is SentVH -> {
                holder.time.text = item.time
                holder.bubble.text = item.text
            }
            is ReceivedVH -> {
                holder.time.text = item.time
                holder.bubble.text = item.text
                holder.avatar.text = peerAvatarLetter
            }
        }
    }

    override fun getItemCount() = items.size

    private class SentVH(view: View) : RecyclerView.ViewHolder(view) {
        val time: TextView = view.findViewById(R.id.time)
        val bubble: TextView = view.findViewById(R.id.bubble)
    }

    private class ReceivedVH(view: View) : RecyclerView.ViewHolder(view) {
        val time: TextView = view.findViewById(R.id.time)
        val bubble: TextView = view.findViewById(R.id.bubble)
        val avatar: TextView = view.findViewById(R.id.avatar)
    }

    private companion object {
        const val TYPE_RECEIVED = 0
        const val TYPE_SENT = 1
    }
}
