package com.example.appdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class RecyclerViewDemoActivity : AppCompatActivity() {

    private lateinit var adapter: DemoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recycler_view_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recycler_demo_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btn_recycler_demo_back).setOnClickListener { finish() }

        val list = findViewById<RecyclerView>(R.id.recycler_demo_list)
        adapter = DemoAdapter(
            banners = DemoData.banners(),
            contents = DemoData.contents().toMutableList(),
            onContentClick = { Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show() }
        )

        val layoutManager = GridLayoutManager(this, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                if (adapter.getItemViewType(position) == DemoAdapter.TYPE_CONTENT) 1 else 2
        }
        list.layoutManager = layoutManager
        list.adapter = adapter
        list.addItemDecoration(ContentSpaceDecoration(spacingDp = 6f))

        attachItemTouchHelper(list)

        findViewById<TextView>(R.id.btn_recycler_demo_reset).setOnClickListener {
            adapter.resetContents(DemoData.contents())
        }
    }

    private fun attachItemTouchHelper(list: RecyclerView) {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun getMovementFlags(rv: RecyclerView, vh: RecyclerView.ViewHolder): Int {
                if (vh.itemViewType != DemoAdapter.TYPE_CONTENT) return 0
                return super.getMovementFlags(rv, vh)
            }

            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (target.itemViewType != DemoAdapter.TYPE_CONTENT) return false
                return adapter.moveContent(vh.bindingAdapterPosition, target.bindingAdapterPosition)
            }

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                adapter.removeContent(vh.bindingAdapterPosition)
            }

            override fun onSelectedChanged(vh: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(vh, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    vh?.itemView?.animate()?.scaleX(1.02f)?.scaleY(1.02f)?.setDuration(120)?.start()
                }
            }

            override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder) {
                super.clearView(rv, vh)
                vh.itemView.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(list)
    }
}

private class ContentSpaceDecoration(spacingDp: Float) : RecyclerView.ItemDecoration() {
    private var spacingPx = 0
    private val spacingDpValue = spacingDp

    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (spacingPx == 0) {
            spacingPx = (spacingDpValue * view.resources.displayMetrics.density).toInt()
        }
        val position = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter ?: return
        if (adapter.getItemViewType(position) != DemoAdapter.TYPE_CONTENT) {
            outRect.setEmpty()
            return
        }
        outRect.top = spacingPx
        outRect.bottom = spacingPx
    }
}

class DemoAdapter(
    private val banners: List<DemoData.Banner>,
    private val contents: MutableList<DemoData.Content>,
    private val onContentClick: (DemoData.Content) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_SECTION_HEADER = 1
        const val TYPE_BANNER_ROW = 2
        const val TYPE_CONTENT = 3
        const val TYPE_FOOTER = 4
    }

    private sealed class Row {
        data class SectionHeader(val title: String, val subtitle: String) : Row()
        data object BannerRow : Row()
        data class Content(val data: DemoData.Content) : Row()
        data object Footer : Row()
    }

    private fun rows(): List<Row> = buildList {
        add(Row.SectionHeader("每日精选", "向右滑动浏览更多卡片"))
        add(Row.BannerRow)
        add(Row.SectionHeader("推荐内容", "长按拖拽排序，左右滑动删除"))
        contents.forEach { add(Row.Content(it)) }
        add(Row.Footer)
    }

    private var rowCache: List<Row> = rows()

    override fun getItemCount(): Int = rowCache.size

    override fun getItemViewType(position: Int): Int = when (rowCache[position]) {
        is Row.SectionHeader -> TYPE_SECTION_HEADER
        Row.BannerRow -> TYPE_BANNER_ROW
        is Row.Content -> TYPE_CONTENT
        Row.Footer -> TYPE_FOOTER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SECTION_HEADER -> SectionHeaderVH(inflater.inflate(R.layout.item_rv_section_header, parent, false))
            TYPE_BANNER_ROW -> BannerRowVH(inflater.inflate(R.layout.item_rv_banner_row, parent, false), banners)
            TYPE_CONTENT -> ContentVH(inflater.inflate(R.layout.item_rv_content, parent, false))
            TYPE_FOOTER -> FooterVH(inflater.inflate(R.layout.item_rv_footer, parent, false))
            else -> error("unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rowCache[position]) {
            is Row.SectionHeader -> (holder as SectionHeaderVH).bind(row.title, row.subtitle)
            Row.BannerRow -> (holder as BannerRowVH).bind()
            is Row.Content -> (holder as ContentVH).bind(row.data, onContentClick)
            Row.Footer -> Unit
        }
    }

    fun moveContent(from: Int, to: Int): Boolean {
        val fromContent = rowCache[from] as? Row.Content ?: return false
        val toContent = rowCache[to] as? Row.Content ?: return false
        val fromIdx = contents.indexOf(fromContent.data)
        val toIdx = contents.indexOf(toContent.data)
        if (fromIdx < 0 || toIdx < 0) return false
        Collections.swap(contents, fromIdx, toIdx)
        rowCache = rows()
        notifyItemMoved(from, to)
        return true
    }

    fun removeContent(position: Int) {
        val row = rowCache.getOrNull(position) as? Row.Content ?: return
        contents.remove(row.data)
        rowCache = rows()
        notifyItemRemoved(position)
    }

    fun resetContents(fresh: List<DemoData.Content>) {
        contents.clear()
        contents.addAll(fresh)
        rowCache = rows()
        notifyDataSetChanged()
    }

    private class SectionHeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.rv_section_title)
        private val subtitle: TextView = view.findViewById(R.id.rv_section_subtitle)
        fun bind(t: String, s: String) {
            title.text = t
            subtitle.text = s
        }
    }

    private class BannerRowVH(view: View, banners: List<DemoData.Banner>) : RecyclerView.ViewHolder(view) {
        init {
            val rv = view as RecyclerView
            rv.layoutManager = LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)
            rv.adapter = BannerAdapter(banners)
            if (rv.onFlingListener == null) {
                androidx.recyclerview.widget.PagerSnapHelper().attachToRecyclerView(rv)
            }
        }
        fun bind() = Unit
    }

    private class ContentVH(view: View) : RecyclerView.ViewHolder(view) {
        private val card: View = view.findViewById(R.id.rv_content_card)
        private val avatar: TextView = view.findViewById(R.id.rv_content_avatar)
        private val title: TextView = view.findViewById(R.id.rv_content_title)
        private val subtitle: TextView = view.findViewById(R.id.rv_content_subtitle)
        private val time: TextView = view.findViewById(R.id.rv_content_time)
        private val tag: TextView = view.findViewById(R.id.rv_content_tag)
        fun bind(data: DemoData.Content, onClick: (DemoData.Content) -> Unit) {
            avatar.text = data.title.firstOrNull()?.toString().orEmpty()
            avatar.backgroundTintList = android.content.res.ColorStateList.valueOf(data.avatarColor)
            title.text = data.title
            subtitle.text = data.subtitle
            time.text = data.time
            tag.text = data.tag
            card.setOnClickListener { onClick(data) }
        }
    }

    private class FooterVH(view: View) : RecyclerView.ViewHolder(view)
}

private class BannerAdapter(private val items: List<DemoData.Banner>) : RecyclerView.Adapter<BannerAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_banner, parent, false)
        return VH(view)
    }
    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val card: View = view.findViewById(R.id.rv_banner_card)
        private val title: TextView = view.findViewById(R.id.rv_banner_title)
        private val subtitle: TextView = view.findViewById(R.id.rv_banner_subtitle)
        private val tag: TextView = view.findViewById(R.id.rv_banner_tag)
        fun bind(data: DemoData.Banner) {
            card.setBackgroundResource(data.background)
            title.text = data.title
            subtitle.text = data.subtitle
            tag.text = data.tag
        }
    }
}

object DemoData {
    data class Banner(val title: String, val subtitle: String, val tag: String, val background: Int)
    data class Content(
        val title: String,
        val subtitle: String,
        val time: String,
        val tag: String,
        val avatarColor: Int,
    )

    fun banners(): List<Banner> = listOf(
        Banner("Jetpack Compose", "声明式 UI 入门到精通", "热门", R.drawable.bg_rv_banner_1),
        Banner("Kotlin 协程", "结构化并发与 Flow 实战", "进阶", R.drawable.bg_rv_banner_2),
        Banner("性能优化", "启动 / 渲染 / 内存全攻略", "实战", R.drawable.bg_rv_banner_3),
        Banner("Material 3", "动态取色与新组件", "设计", R.drawable.bg_rv_banner_4),
    )

    fun contents(): List<Content> = listOf(
        Content("RecyclerView 多类型布局", "用 getItemViewType 区分 Header / Banner / Content / Footer", "08:24", "RecyclerView", 0xFFFFD8A8.toInt()),
        Content("ItemTouchHelper 拖拽与滑动", "通过 SimpleCallback 实现长按拖拽和左右滑动删除", "09:10", "交互", 0xFFB8E0FF.toInt()),
        Content("DiffUtil 差分更新", "替代 notifyDataSetChanged，提升列表刷新效率", "10:32", "性能", 0xFFFFC8DD.toInt()),
        Content("GridLayoutManager 混排", "通过 SpanSizeLookup 让 Header 占满整行", "11:48", "布局", 0xFFC3F0CA.toInt()),
        Content("ItemDecoration 自定义间距", "用 getItemOffsets 优雅地控制 item 间距", "13:05", "细节", 0xFFE0D4FF.toInt()),
        Content("SnapHelper 吸附", "PagerSnapHelper 让横向滑动停在卡片边界", "14:22", "动效", 0xFFFFD8A8.toInt()),
        Content("Adapter 嵌套 RecyclerView", "外层纵向 + 内层横向，组合更丰富的页面", "15:30", "组合", 0xFFB8E0FF.toInt()),
        Content("Edge-to-Edge 适配", "WindowInsets 处理状态栏与导航栏", "16:45", "适配", 0xFFFFC8DD.toInt()),
    )
}
