package com.example.appdemo

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TimelineDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo_timeline)
        setupDemoToolbar(R.string.timeline_title)

        val list = findViewById<RecyclerView>(R.id.timeline_list)
        list.layoutManager = LinearLayoutManager(this)
        val items = TimelineData.items()
        list.adapter = TimelineAdapter(items)
        list.addItemDecoration(TimelineDecoration(this, items))
        list.itemAnimator = TimelineItemAnimator()
    }
}

data class TimelineItem(val title: String, val time: String, val desc: String, val color: Int)

object TimelineData {
    fun items(): List<TimelineItem> = listOf(
        TimelineItem("项目启动", "09:00", "和团队一起对齐目标与里程碑。", 0xFF07C160.toInt()),
        TimelineItem("需求评审", "10:30", "评估范围、优先级与风险。", 0xFF6A8DFF.toInt()),
        TimelineItem("设计稿走查", "13:30", "确认交互细节，沉淀组件库。", 0xFFFF8A65.toInt()),
        TimelineItem("接口联调", "15:00", "联调主流程接口，记录边界。", 0xFF22C1C3.toInt()),
        TimelineItem("性能调试", "17:00", "Profiler 抓取关键路径耗时。", 0xFFFFB94B.toInt()),
        TimelineItem("代码评审", "19:30", "提交 PR，等待评审反馈。", 0xFF8E66FF.toInt()),
        TimelineItem("回归测试", "21:00", "覆盖核心路径，回归无问题。", 0xFFFF5E8C.toInt()),
        TimelineItem("提交日报", "22:30", "总结今日进展并规划明日。", 0xFF3CCB8A.toInt()),
    )
}

private class TimelineAdapter(val items: List<TimelineItem>) : RecyclerView.Adapter<TimelineAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_demo_timeline, parent, false)
        return VH(v)
    }
    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val card: View = view.findViewById(R.id.timeline_card)
        private val title: TextView = view.findViewById(R.id.timeline_title)
        private val time: TextView = view.findViewById(R.id.timeline_time)
        private val desc: TextView = view.findViewById(R.id.timeline_desc)
        fun bind(it: TimelineItem) {
            title.text = it.title
            time.text = it.time
            desc.text = it.desc
            card.setOnClickListener {
                card.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction {
                    card.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }.start()
            }
        }
    }
}

private class TimelineDecoration(
    ctx: android.content.Context,
    private val items: List<TimelineItem>,
) : RecyclerView.ItemDecoration() {

    private val density = ctx.resources.displayMetrics.density
    private val lineX = 28f * density
    private val dotRadius = 7f * density
    private val ringRadius = 11f * density
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ctx.getColor(R.color.demo_timeline_line)
        strokeWidth = 2f * density
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.setEmpty()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val pos = parent.getChildAdapterPosition(child)
            if (pos == RecyclerView.NO_POSITION) continue
            val item = items[pos]
            val centerY = child.top + child.height / 2f
            if (pos != 0) c.drawLine(lineX, child.top.toFloat(), lineX, centerY - ringRadius, linePaint)
            if (pos != items.lastIndex) c.drawLine(lineX, centerY + ringRadius, lineX, child.bottom.toFloat(), linePaint)
            ringPaint.color = (item.color and 0x00FFFFFF) or 0x33000000.toInt()
            c.drawCircle(lineX, centerY, ringRadius, ringPaint)
            dotPaint.color = item.color
            c.drawCircle(lineX, centerY, dotRadius, dotPaint)
        }
    }
}

private class TimelineItemAnimator : androidx.recyclerview.widget.DefaultItemAnimator() {
    init { addDuration = 280 }
    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.translationX = -40f * holder.itemView.resources.displayMetrics.density
        holder.itemView.alpha = 0f
        holder.itemView.animate().translationX(0f).alpha(1f)
            .setDuration(addDuration)
            .withEndAction { dispatchAddFinished(holder) }
            .start()
        return true
    }
}
