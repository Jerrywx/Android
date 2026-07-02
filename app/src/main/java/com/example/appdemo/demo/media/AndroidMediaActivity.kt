package com.example.appdemo.demo.media

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
 * 音视频模块总入口。
 *
 *   1. TTS 朗读跟随：TextToSpeech + UtteranceProgressListener 精准字符高亮
 *   （后续追加：MediaPlayer + LRC、ExoPlayer、CameraX 拍照录像、录音…）
 */
class AndroidMediaActivity : AppCompatActivity() {

    private data class NavItem(val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_media)
        setupDemoToolbar(R.string.media_title, R.id.android_media_root)

        val entries = listOf(
            NavItem(
                getString(R.string.media_tts),
                getString(R.string.media_tts_hint),
                TtsReaderDemoActivity::class.java,
            ),
            NavItem(
                getString(R.string.media_lrc),
                getString(R.string.media_lrc_hint),
                LrcPlayerDemoActivity::class.java,
            ),
        )

        val list = findViewById<RecyclerView>(R.id.android_media_list)
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
