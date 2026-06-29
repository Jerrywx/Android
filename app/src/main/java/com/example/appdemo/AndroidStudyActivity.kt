package com.example.appdemo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AndroidStudyActivity : AppCompatActivity() {

    private data class Entry(val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_study)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.android_study_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btn_android_study_back).setOnClickListener { finish() }

        val entries = listOf(
            Entry(getString(R.string.android_study_recycler_view), "基础多类型 / 拖拽 / 滑动删除", RecyclerViewDemoActivity::class.java),
            Entry(getString(R.string.android_study_recycler_view_pro), "DiffUtil / Payload / 多 Layout 切换", RecyclerViewDemoActivityPro::class.java),
            Entry(getString(R.string.demo_gallery), getString(R.string.gallery_hint), GalleryDemoActivity::class.java),
            Entry(getString(R.string.demo_chat), getString(R.string.chat_demo_hint), ChatDemoActivity::class.java),
            Entry(getString(R.string.demo_sticky), getString(R.string.sticky_hint), StickyHeaderDemoActivity::class.java),
            Entry(getString(R.string.demo_nested), getString(R.string.nested_hint), NestedHomeDemoActivity::class.java),
            Entry(getString(R.string.demo_concat), getString(R.string.concat_hint), ConcatAdapterDemoActivity::class.java),
            Entry(getString(R.string.demo_timeline), getString(R.string.timeline_hint), TimelineDemoActivity::class.java),
            Entry(getString(R.string.demo_fast_scroller), getString(R.string.fast_scroller_hint), FastScrollerDemoActivity::class.java),
            Entry(getString(R.string.demo_video_feed), getString(R.string.video_feed_hint), VideoFeedDemoActivity::class.java),
            Entry(getString(R.string.demo_tag_flow), getString(R.string.tag_flow_hint), TagFlowDemoActivity::class.java),
            Entry(getString(R.string.demo_search_filter), getString(R.string.search_filter_hint), SearchFilterDemoActivity::class.java),
            Entry(getString(R.string.demo_loop_banner), getString(R.string.loop_banner_hint), LoopBannerDemoActivity::class.java),
        )

        val list = findViewById<RecyclerView>(R.id.study_list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = EntryAdapter(entries) { entry ->
            startActivity(Intent(this, entry.target))
        }
    }

    private class EntryAdapter(
        private val items: List<Entry>,
        private val onClick: (Entry) -> Unit,
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
            fun bind(entry: Entry, no: Int, onClick: (Entry) -> Unit) {
                index.text = no.toString()
                title.text = entry.title
                subtitle.text = entry.subtitle
                card.setOnClickListener { onClick(entry) }
            }
        }
    }
}
