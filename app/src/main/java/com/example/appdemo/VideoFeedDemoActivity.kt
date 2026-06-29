package com.example.appdemo

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

class VideoFeedDemoActivity : AppCompatActivity() {

    private val items = VideoFeedData.items().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo_video)
        setupDemoToolbar(R.string.video_feed_title)

        val list = findViewById<RecyclerView>(R.id.video_list)
        list.layoutManager = LinearLayoutManager(this)
        val adapter = VideoFeedAdapter(items)
        list.adapter = adapter
        PagerSnapHelper().attachToRecyclerView(list)

        var currentPos = RecyclerView.NO_POSITION
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                val lm = rv.layoutManager as LinearLayoutManager
                val pos = lm.findFirstCompletelyVisibleItemPosition()
                if (pos == RecyclerView.NO_POSITION || pos == currentPos) return
                currentPos = pos
                val vh = rv.findViewHolderForAdapterPosition(pos) as? VideoFeedAdapter.VH
                vh?.playEnterAnimation()
            }
        })
    }
}

object VideoFeedData {
    data class Item(val author: String, val title: String, val desc: String, val likes: Int, val liked: Boolean, val start: Int, val end: Int)
    fun items(): List<Item> = listOf(
        Item("@Compose 大师", "声明式 UI 一分钟看懂", "用一个例子说清楚 Compose 与 View 的差异。", 1234, false, 0xFF6A8DFF.toInt(), 0xFF8E66FF.toInt()),
        Item("@协程小王", "结构化并发", "为什么协程让并发更简单？", 988, true, 0xFFFF8A65.toInt(), 0xFFFF5E8C.toInt()),
        Item("@性能老张", "启动优化全攻略", "冷启动、热启动、首屏渲染怎么调？", 2310, false, 0xFF22C1C3.toInt(), 0xFF3CCB8A.toInt()),
        Item("@动画 Jane", "MotionLayout 实战", "用 5 分钟做出抖音点赞动画。", 1530, false, 0xFFFFB94B.toInt(), 0xFFFF7E5F.toInt()),
        Item("@设计 Iris", "Material 3 配色", "动态取色三步走。", 786, true, 0xFFA18CD1.toInt(), 0xFFFBC2EB.toInt()),
        Item("@架构 Sam", "组件化项目", "Demo 工程到大型 App 的演进。", 1722, false, 0xFF373B44.toInt(), 0xFF4286F4.toInt()),
    )
}

private class VideoFeedAdapter(val items: MutableList<VideoFeedData.Item>) : RecyclerView.Adapter<VideoFeedAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_demo_video, parent, false)
        v.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        return VH(v)
    }
    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position], position) { idx, liked ->
        items[idx] = items[idx].copy(
            liked = liked,
            likes = items[idx].likes + if (liked) 1 else -1,
        )
        notifyItemChanged(idx, "like")
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
        else holder.bindLike(items[position], animate = true)
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val bg: View = view.findViewById(R.id.video_bg)
        private val author: TextView = view.findViewById(R.id.video_author)
        private val title: TextView = view.findViewById(R.id.video_title)
        private val desc: TextView = view.findViewById(R.id.video_desc)
        private val like: ImageView = view.findViewById(R.id.video_like)
        private val likes: TextView = view.findViewById(R.id.video_likes)
        private val play: ImageView = view.findViewById(R.id.video_play)

        fun bind(item: VideoFeedData.Item, position: Int, onLike: (Int, Boolean) -> Unit) {
            bg.background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(item.start, item.end))
            author.text = item.author
            title.text = item.title
            desc.text = item.desc
            bindLike(item, animate = false)
            like.setOnClickListener { onLike(position, !item.liked) }
            itemView.setOnClickListener { showPlayPulse() }
        }

        fun bindLike(item: VideoFeedData.Item, animate: Boolean) {
            like.setImageResource(if (item.liked) R.drawable.ic_heart else R.drawable.ic_heart_outline)
            like.imageTintList = ColorStateList.valueOf(
                if (item.liked) like.context.getColor(R.color.pro_like) else like.context.getColor(R.color.white)
            )
            likes.text = item.likes.toString()
            if (animate) {
                like.animate().cancel()
                like.scaleX = 0.6f; like.scaleY = 0.6f
                like.animate().scaleX(1f).scaleY(1f).setDuration(280)
                    .setInterpolator(OvershootInterpolator(3f)).start()
            }
        }

        fun playEnterAnimation() {
            title.alpha = 0f; title.translationY = 30f
            desc.alpha = 0f; desc.translationY = 30f
            author.alpha = 0f; author.translationY = 30f
            title.animate().alpha(1f).translationY(0f).setStartDelay(60).setDuration(360).start()
            desc.animate().alpha(1f).translationY(0f).setStartDelay(120).setDuration(360).start()
            author.animate().alpha(1f).translationY(0f).setStartDelay(0).setDuration(360).start()
            showPlayPulse()
        }

        private fun showPlayPulse() {
            play.animate().cancel()
            play.alpha = 0f; play.scaleX = 0.6f; play.scaleY = 0.6f
            play.animate().alpha(0.8f).scaleX(1.1f).scaleY(1.1f).setDuration(200).withEndAction {
                play.animate().alpha(0f).setStartDelay(180).setDuration(220).start()
            }.start()
        }
    }
}
