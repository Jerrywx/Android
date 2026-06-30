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

class AndroidViewPager2Activity : AppCompatActivity() {

    private data class Entry(val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_view_pager2)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.android_vp2_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btn_android_vp2_back).setOnClickListener { finish() }

        val entries = listOf(
            Entry(getString(R.string.vp2_demo_onboarding), getString(R.string.vp2_demo_onboarding_hint), Vp2OnboardingDemoActivity::class.java),
            Entry(getString(R.string.vp2_demo_tab), getString(R.string.vp2_demo_tab_hint), Vp2TabDemoActivity::class.java),
            Entry(getString(R.string.vp2_demo_gallery), getString(R.string.vp2_demo_gallery_hint), Vp2GalleryDemoActivity::class.java),
            Entry(getString(R.string.vp2_demo_vertical), getString(R.string.vp2_demo_vertical_hint), Vp2VerticalFeedDemoActivity::class.java),
            Entry(getString(R.string.vp2_demo_card), getString(R.string.vp2_demo_card_hint), Vp2CardStackDemoActivity::class.java),
            Entry(getString(R.string.vp2_demo_wizard), getString(R.string.vp2_demo_wizard_hint), Vp2WizardDemoActivity::class.java),
            Entry(getString(R.string.vp2_demo_banner), getString(R.string.vp2_demo_banner_hint), Vp2BannerDemoActivity::class.java),
        )

        val list = findViewById<RecyclerView>(R.id.android_vp2_list)
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
        override fun onBindViewHolder(holder: VH, position: Int) =
            holder.bind(items[position], position + 1, onClick)

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
