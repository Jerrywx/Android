package com.example.appdemo.demo.viewpager

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

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class Vp2VerticalFeedDemoActivity : AppCompatActivity() {

    data class Video(
        val author: String,
        val desc: String,
        val music: String,
        val emoji: String,
        val start: Int,
        val end: Int,
        var likes: Int,
        var liked: Boolean,
    )

    private val items = mutableListOf(
        Video("@compose_dev", "Compose 写一个会跳舞的 FAB ✨", "♬ Lo-Fi · Chillhop Mix", "🪩", 0xFF6A8DFF.toInt(), 0xFF8E66FF.toInt(), 1248, false),
        Video("@coroutine_master", "结构化并发到底是什么？三分钟讲透", "♬ Synthwave · NightDrive", "🧵", 0xFFFF8A65.toInt(), 0xFFFF5E8C.toInt(), 882, false),
        Video("@motion_lab", "MotionLayout 实战：抖动按钮", "♬ Funky Beats", "🎞", 0xFF22C1C3.toInt(), 0xFF3CCB8A.toInt(), 2014, true),
        Video("@perf_team", "启动优化：从 3s 到 800ms", "♬ Cinematic Build-up", "🚀", 0xFFFFB94B.toInt(), 0xFFFF7E5F.toInt(), 5237, false),
        Video("@material3", "动态取色 Demo · 一键换肤", "♬ Indie Pop", "🌈", 0xFFA18CD1.toInt(), 0xFFFBC2EB.toInt(), 763, false),
        Video("@nav_guru", "单 Activity + Navigation 入门", "♬ Lo-Fi Beats", "🧭", 0xFF4FACFE.toInt(), 0xFF00F2FE.toInt(), 1500, false),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vp2_vertical)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.vp2_v_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        findViewById<ImageButton>(R.id.vp2_v_back).setOnClickListener { finish() }

        val pager = findViewById<ViewPager2>(R.id.vp2_v_pager)
        pager.orientation = ViewPager2.ORIENTATION_VERTICAL
        pager.adapter = FeedAdapter(items)
        pager.offscreenPageLimit = 1
        pager.setPageTransformer { page, position ->
            val abs = kotlin.math.abs(position)
            page.alpha = 1f - abs * 0.4f
            val emoji = page.findViewById<TextView>(R.id.vp2_v_emoji)
            emoji?.scaleX = 1f - abs * 0.1f
            emoji?.scaleY = 1f - abs * 0.1f
            emoji?.translationY = -position * page.height * 0.15f
        }
    }

    private class FeedAdapter(val items: MutableList<Video>) : RecyclerView.Adapter<FeedAdapter.VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vp2_vertical, parent, false)
            return VH(v)
        }
        override fun getItemCount(): Int = items.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            private val root: FrameLayout = v.findViewById(R.id.vp2_v_item_root)
            private val emoji: TextView = v.findViewById(R.id.vp2_v_emoji)
            private val author: TextView = v.findViewById(R.id.vp2_v_author)
            private val desc: TextView = v.findViewById(R.id.vp2_v_desc)
            private val music: TextView = v.findViewById(R.id.vp2_v_music)
            private val likeIcon: ImageView = v.findViewById(R.id.vp2_v_like_icon)
            private val likeCount: TextView = v.findViewById(R.id.vp2_v_like_count)
            private val burst: ImageView = v.findViewById(R.id.vp2_v_burst_heart)

            fun bind(item: Video) {
                emoji.text = item.emoji
                author.text = item.author
                desc.text = item.desc
                music.text = item.music
                likeCount.text = item.likes.toString()
                likeIcon.setColorFilter(
                    if (item.liked) 0xFFFF4D6D.toInt() else 0xFFFFFFFF.toInt()
                )
                val d = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(item.start, item.end))
                root.background = d

                val gesture = GestureDetectorCompat(itemView.context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        if (!item.liked) {
                            item.liked = true
                            item.likes += 1
                            likeCount.text = item.likes.toString()
                            likeIcon.setColorFilter(0xFFFF4D6D.toInt())
                        }
                        playBurst()
                        return true
                    }
                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean = true
                })
                root.setOnTouchListener { _, ev -> gesture.onTouchEvent(ev); true }

                likeIcon.setOnClickListener {
                    item.liked = !item.liked
                    item.likes += if (item.liked) 1 else -1
                    likeCount.text = item.likes.toString()
                    likeIcon.setColorFilter(if (item.liked) 0xFFFF4D6D.toInt() else 0xFFFFFFFF.toInt())
                    likeIcon.animate().cancel()
                    likeIcon.scaleX = 0.7f; likeIcon.scaleY = 0.7f
                    likeIcon.animate().scaleX(1f).scaleY(1f).setDuration(220).start()
                }
            }

            private fun playBurst() {
                burst.alpha = 0f
                burst.scaleX = 0.3f
                burst.scaleY = 0.3f
                burst.animate().cancel()
                burst.animate().alpha(1f).scaleX(1.4f).scaleY(1.4f).setDuration(220)
                    .withEndAction {
                        burst.animate().alpha(0f).scaleX(1.8f).scaleY(1.8f).setDuration(260).start()
                    }.start()
            }
        }
    }
}
