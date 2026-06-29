package com.example.appdemo

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class RecyclerViewDemoActivityPro : AppCompatActivity() {

    enum class LayoutMode { LIST, GRID, STAGGER }
    private enum class Filter { ALL, STARRED, RECENT }

    private lateinit var listView: RecyclerView
    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var actionBar: LinearLayout
    private lateinit var actionCount: TextView
    private lateinit var chipGroup: LinearLayout
    private lateinit var adapter: ProAdapter

    private val chipsViews = mutableMapOf<Filter, TextView>()
    private val toggleSegments = mutableMapOf<LayoutMode, ImageView>()

    private var layoutMode = LayoutMode.LIST
    private var filter = Filter.ALL
    private var multiSelect = false
    private var loadingMore = false
    private var page = 0
    private val pageSize = 12
    private val allItems = ProData.generate(40).toMutableList()
    private val loaded = mutableListOf<ProData.Item>()
    private val selected = mutableSetOf<Long>()

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recycler_view_demo_pro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.pro_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btn_pro_back).setOnClickListener { finish() }

        listView = findViewById(R.id.pro_list)
        refresh = findViewById(R.id.pro_refresh)
        actionBar = findViewById(R.id.pro_action_bar)
        actionCount = findViewById(R.id.pro_action_count)
        chipGroup = findViewById(R.id.pro_chip_group)

        bindToggle()
        bindChips()
        bindActionBar()
        bindRefresh()
        bindList()

        loadPage(reset = true)
    }

    private fun bindList() {
        adapter = ProAdapter(
            onClick = { item, vh ->
                if (multiSelect) toggleSelection(item) else animateClick(vh.itemView)
            },
            onLongPress = { item ->
                if (!multiSelect) enterMultiSelect()
                toggleSelection(item)
            },
            onLikeClick = { item -> toggleLike(item) },
            isSelected = { selected.contains(it.id) },
            currentLayout = { layoutMode },
        )
        applyLayoutManager()
        listView.adapter = adapter
        listView.itemAnimator = SlideUpItemAnimator()
        listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0 || loadingMore) return
                val lm = rv.layoutManager ?: return
                val total = lm.itemCount
                val lastVisible = when (lm) {
                    is LinearLayoutManager -> lm.findLastVisibleItemPosition()
                    is GridLayoutManager -> lm.findLastVisibleItemPosition()
                    is StaggeredGridLayoutManager -> {
                        val arr = IntArray(lm.spanCount)
                        lm.findLastVisibleItemPositions(arr)
                        arr.maxOrNull() ?: 0
                    }
                    else -> 0
                }
                if (lastVisible >= total - 3) loadPage(reset = false)
            }
        })
    }

    private fun bindToggle() {
        toggleSegments[LayoutMode.LIST] = findViewById(R.id.pro_toggle_list)
        toggleSegments[LayoutMode.GRID] = findViewById(R.id.pro_toggle_grid)
        toggleSegments[LayoutMode.STAGGER] = findViewById(R.id.pro_toggle_stagger)
        toggleSegments.forEach { (mode, view) ->
            view.setOnClickListener { switchLayout(mode) }
        }
        updateToggleSelection()
    }

    private fun bindChips() {
        listOf(
            Filter.ALL to getString(R.string.rvpro_chip_all),
            Filter.STARRED to getString(R.string.rvpro_chip_starred),
            Filter.RECENT to getString(R.string.rvpro_chip_recent),
        ).forEach { (f, label) ->
            val chip = LayoutInflater.from(this).inflate(R.layout.item_pro_chip, chipGroup, false) as TextView
            chip.text = label
            chip.isSelected = f == filter
            chip.setOnClickListener { switchFilter(f) }
            chipGroup.addView(chip)
            chipsViews[f] = chip
        }
    }

    private fun bindActionBar() {
        findViewById<TextView>(R.id.pro_action_delete).setOnClickListener { deleteSelected() }
        findViewById<TextView>(R.id.pro_action_cancel).setOnClickListener { exitMultiSelect() }
    }

    private fun bindRefresh() {
        refresh.setColorSchemeColors(getColor(R.color.pro_accent))
        refresh.setOnRefreshListener {
            mainHandler.postDelayed({
                allItems.clear()
                allItems.addAll(ProData.generate(40))
                page = 0
                loaded.clear()
                loadPage(reset = true)
                refresh.isRefreshing = false
            }, 700)
        }
    }

    private fun applyLayoutManager() {
        listView.layoutManager = when (layoutMode) {
            LayoutMode.LIST -> LinearLayoutManager(this)
            LayoutMode.GRID -> GridLayoutManager(this, 2)
            LayoutMode.STAGGER -> StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            }
        }
    }

    private fun switchLayout(mode: LayoutMode) {
        if (mode == layoutMode) return
        layoutMode = mode
        applyLayoutManager()
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
        updateToggleSelection()
    }

    private fun updateToggleSelection() {
        toggleSegments.forEach { (mode, view) -> view.isSelected = mode == layoutMode }
    }

    private fun switchFilter(f: Filter) {
        if (f == filter) return
        filter = f
        chipsViews.forEach { (k, v) -> v.isSelected = k == filter }
        page = 0
        loaded.clear()
        loadPage(reset = true)
    }

    private fun visibleSource(): List<ProData.Item> = when (filter) {
        Filter.ALL -> allItems
        Filter.STARRED -> allItems.filter { it.liked }
        Filter.RECENT -> allItems.sortedByDescending { it.createdAt }
    }

    private fun loadPage(reset: Boolean) {
        if (loadingMore) return
        val source = visibleSource()
        if (reset) {
            page = 0
            loaded.clear()
            val to = pageSize.coerceAtMost(source.size)
            loaded.addAll(source.subList(0, to))
            page = 1
            val done = page * pageSize >= source.size
            adapter.submit(buildRows(loading = false, done = done))
            return
        }
        val from = page * pageSize
        if (from >= source.size) {
            adapter.submit(buildRows(loading = false, done = true))
            return
        }
        loadingMore = true
        adapter.submit(buildRows(loading = true)) {
            mainHandler.postDelayed({
                val to = (from + pageSize).coerceAtMost(source.size)
                loaded.addAll(source.subList(from, to))
                page++
                loadingMore = false
                val done = page * pageSize >= source.size
                adapter.submit(buildRows(loading = false, done = done))
            }, 600)
        }
    }

    private fun buildRows(loading: Boolean, done: Boolean = false): List<ProAdapter.Row> = buildList {
        loaded.forEach { add(ProAdapter.Row.Content(it)) }
        if (loading) add(ProAdapter.Row.Footer(ProAdapter.FooterState.LOADING))
        else if (done && loaded.isNotEmpty()) add(ProAdapter.Row.Footer(ProAdapter.FooterState.DONE))
    }

    private fun toggleLike(item: ProData.Item) {
        val idx = allItems.indexOfFirst { it.id == item.id }
        if (idx < 0) return
        val updated = allItems[idx].copy(
            liked = !allItems[idx].liked,
            likes = allItems[idx].likes + if (allItems[idx].liked) -1 else 1,
        )
        allItems[idx] = updated
        val loadedIdx = loaded.indexOfFirst { it.id == item.id }
        if (loadedIdx >= 0) loaded[loadedIdx] = updated
        adapter.submit(buildRows(loading = false, done = page * pageSize >= visibleSource().size))
    }

    private fun toggleSelection(item: ProData.Item) {
        if (selected.contains(item.id)) selected.remove(item.id) else selected.add(item.id)
        if (selected.isEmpty()) {
            exitMultiSelect()
        } else {
            actionCount.text = getString(R.string.rvpro_selection_count, selected.size)
            adapter.notifyItemRangeChanged(0, adapter.itemCount, PAYLOAD_SELECTION)
        }
    }

    private fun enterMultiSelect() {
        if (multiSelect) return
        multiSelect = true
        actionBar.visibility = View.VISIBLE
        actionBar.alpha = 0f
        actionBar.translationY = -actionBar.height.toFloat().coerceAtLeast(40f)
        actionBar.animate().alpha(1f).translationY(0f).setDuration(180).start()
    }

    private fun exitMultiSelect() {
        if (!multiSelect) return
        multiSelect = false
        selected.clear()
        actionBar.animate().alpha(0f).translationY(-40f).setDuration(160).withEndAction {
            actionBar.visibility = View.GONE
        }.start()
        adapter.notifyItemRangeChanged(0, adapter.itemCount, PAYLOAD_SELECTION)
    }

    private fun deleteSelected() {
        if (selected.isEmpty()) return
        allItems.removeAll { selected.contains(it.id) }
        loaded.removeAll { selected.contains(it.id) }
        selected.clear()
        exitMultiSelect()
        adapter.submit(buildRows(loading = false, done = page * pageSize >= visibleSource().size))
    }

    private fun animateClick(view: View) {
        view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
        }.start()
    }

    override fun onBackPressed() {
        if (multiSelect) exitMultiSelect() else super.onBackPressed()
    }

    companion object {
        const val PAYLOAD_LIKE = "payload_like"
        const val PAYLOAD_SELECTION = "payload_selection"
    }
}

private class ProAdapter(
    private val onClick: (ProData.Item, RecyclerView.ViewHolder) -> Unit,
    private val onLongPress: (ProData.Item) -> Unit,
    private val onLikeClick: (ProData.Item) -> Unit,
    private val isSelected: (ProData.Item) -> Boolean,
    private val currentLayout: () -> RecyclerViewDemoActivityPro.LayoutMode,
) : ListAdapter<ProAdapter.Row, RecyclerView.ViewHolder>(DIFF) {

    sealed class Row {
        abstract val key: Any
        data class Content(val item: ProData.Item) : Row() { override val key: Any = item.id }
        data class Footer(val state: FooterState) : Row() { override val key: Any = "footer" }
    }

    enum class FooterState { LOADING, DONE }

    companion object {
        const val TYPE_CONTENT_LIST = 1
        const val TYPE_CONTENT_GRID = 2
        const val TYPE_CONTENT_STAGGER = 3
        const val TYPE_FOOTER = 9

        private val DIFF = object : DiffUtil.ItemCallback<Row>() {
            override fun areItemsTheSame(o: Row, n: Row) = o.key == n.key
            override fun areContentsTheSame(o: Row, n: Row) = o == n
            override fun getChangePayload(o: Row, n: Row): Any? {
                if (o is Row.Content && n is Row.Content && o.item.id == n.item.id) {
                    if (o.item.liked != n.item.liked || o.item.likes != n.item.likes) {
                        return RecyclerViewDemoActivityPro.PAYLOAD_LIKE
                    }
                }
                return null
            }
        }
    }

    fun submit(rows: List<Row>, commit: (() -> Unit)? = null) {
        if (commit != null) submitList(rows, Runnable { commit() }) else submitList(rows)
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is Row.Footer -> TYPE_FOOTER
        is Row.Content -> when (currentLayout()) {
            RecyclerViewDemoActivityPro.LayoutMode.LIST -> TYPE_CONTENT_LIST
            RecyclerViewDemoActivityPro.LayoutMode.GRID -> TYPE_CONTENT_GRID
            RecyclerViewDemoActivityPro.LayoutMode.STAGGER -> TYPE_CONTENT_STAGGER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_FOOTER -> FooterVH(inflater.inflate(R.layout.item_pro_footer, parent, false))
            TYPE_CONTENT_LIST -> ContentVH(inflater.inflate(R.layout.item_pro_list, parent, false))
            TYPE_CONTENT_GRID -> ContentVH(inflater.inflate(R.layout.item_pro_grid, parent, false))
            TYPE_CONTENT_STAGGER -> ContentVH(inflater.inflate(R.layout.item_pro_stagger, parent, false), isStagger = true)
            else -> error("unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is Row.Content -> (holder as ContentVH).bind(row.item, isSelected(row.item), onClick, onLongPress, onLikeClick)
            is Row.Footer -> (holder as FooterVH).bind(row.state)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        val row = getItem(position)
        if (holder is ContentVH && row is Row.Content) {
            payloads.forEach { p ->
                when (p) {
                    RecyclerViewDemoActivityPro.PAYLOAD_LIKE -> holder.bindLike(row.item, animate = true)
                    RecyclerViewDemoActivityPro.PAYLOAD_SELECTION -> holder.bindSelection(isSelected(row.item))
                }
            }
        }
    }

    class ContentVH(view: View, private val isStagger: Boolean = false) : RecyclerView.ViewHolder(view) {
        private val card: View = view.findViewById(R.id.pro_card)
        private val thumb: TextView = view.findViewById(R.id.pro_thumb)
        private val title: TextView = view.findViewById(R.id.pro_title)
        private val subtitle: TextView = view.findViewById(R.id.pro_subtitle)
        private val category: TextView = view.findViewById(R.id.pro_category)
        private val likes: TextView = view.findViewById(R.id.pro_likes)
        private val like: ImageView = view.findViewById(R.id.pro_like)

        fun bind(
            item: ProData.Item,
            selected: Boolean,
            onClick: (ProData.Item, RecyclerView.ViewHolder) -> Unit,
            onLongPress: (ProData.Item) -> Unit,
            onLikeClick: (ProData.Item) -> Unit,
        ) {
            thumb.text = item.title.firstOrNull()?.toString().orEmpty()
            thumb.background = roundedDrawable(item.color)
            title.text = item.title
            subtitle.text = item.subtitle
            subtitle.maxLines = if (isStagger) item.subtitleLines else subtitle.maxLines
            category.text = item.category
            bindLike(item, animate = false)
            bindSelection(selected)

            card.setOnClickListener { onClick(item, this) }
            card.setOnLongClickListener { onLongPress(item); true }
            like.setOnClickListener { onLikeClick(item) }
        }

        fun bindLike(item: ProData.Item, animate: Boolean) {
            like.setImageResource(if (item.liked) R.drawable.ic_heart else R.drawable.ic_heart_outline)
            like.imageTintList = ColorStateList.valueOf(
                if (item.liked) like.context.getColor(R.color.pro_like)
                else like.context.getColor(R.color.pro_hint)
            )
            likes.text = item.likes.toString()
            if (animate) {
                like.animate().cancel()
                like.scaleX = 0.6f
                like.scaleY = 0.6f
                like.animate().scaleX(1f).scaleY(1f).setDuration(260)
                    .setInterpolator(OvershootInterpolator(3f)).start()
            }
        }

        fun bindSelection(selected: Boolean) {
            card.isSelected = selected
            card.animate().cancel()
            card.animate().scaleX(if (selected) 0.97f else 1f).scaleY(if (selected) 0.97f else 1f)
                .setDuration(140).start()
        }

        private fun roundedDrawable(color: Int): android.graphics.drawable.Drawable {
            val d = android.graphics.drawable.GradientDrawable()
            d.cornerRadius = 14f * card.resources.displayMetrics.density
            d.setColor(color)
            return d
        }
    }

    class FooterVH(view: View) : RecyclerView.ViewHolder(view) {
        private val progress: ProgressBar = view.findViewById(R.id.pro_footer_progress)
        private val text: TextView = view.findViewById(R.id.pro_footer_text)
        fun bind(state: FooterState) {
            when (state) {
                FooterState.LOADING -> {
                    progress.visibility = View.VISIBLE
                    text.text = text.context.getString(R.string.rvpro_footer_loading)
                }
                FooterState.DONE -> {
                    progress.visibility = View.GONE
                    text.text = text.context.getString(R.string.rvpro_footer_done)
                }
            }
        }
    }
}

private class SlideUpItemAnimator : DefaultItemAnimator() {
    init { addDuration = 260; removeDuration = 200; moveDuration = 220; changeDuration = 200 }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.translationY = 60f * holder.itemView.resources.displayMetrics.density
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .translationY(0f).alpha(1f)
            .setDuration(addDuration)
            .withEndAction { dispatchAddFinished(holder) }
            .start()
        return true
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.animate()
            .alpha(0f).translationX(holder.itemView.width * 0.4f)
            .setDuration(removeDuration)
            .withEndAction {
                holder.itemView.alpha = 1f
                holder.itemView.translationX = 0f
                dispatchRemoveFinished(holder)
            }.start()
        return true
    }
}

object ProData {
    data class Item(
        val id: Long,
        val title: String,
        val subtitle: String,
        val category: String,
        val color: Int,
        val likes: Int,
        val liked: Boolean,
        val createdAt: Long,
        val subtitleLines: Int,
    )

    private val categories = listOf("Compose", "协程", "性能", "动画", "适配", "Material", "Jetpack", "测试")
    private val titles = listOf(
        "重学 RecyclerView", "ListAdapter + DiffUtil 实战", "ItemAnimator 自定义入场",
        "StaggeredGridLayoutManager 瀑布流", "嵌套滚动与 SnapHelper", "Payload 局部刷新",
        "多选模式与状态恢复", "下拉刷新 + 上拉加载", "复杂卡片与可变高度",
        "Edge-to-Edge 与系统栏", "Kotlin Flow 与 UI 状态", "协程取消的边界",
        "WindowInsets 进阶", "Material 3 动态色彩", "可访问性优化",
    )
    private val palette = listOf(
        0xFF6A8DFF.toInt(), 0xFFFF8A65.toInt(), 0xFF22C1C3.toInt(),
        0xFFFFB94B.toInt(), 0xFF8E66FF.toInt(), 0xFFFF5E8C.toInt(),
        0xFF3CCB8A.toInt(), 0xFFFF7E5F.toInt(),
    )

    fun generate(count: Int): List<Item> {
        val rnd = java.util.Random(42)
        val now = System.currentTimeMillis()
        return List(count) { i ->
            val title = titles[i % titles.size] + " #${i + 1}"
            val cat = categories[i % categories.size]
            val color = palette[i % palette.size]
            val subtitleBase = "演示 RecyclerView 的进阶能力，结合 DiffUtil/ItemAnimator/Payload 完成局部刷新与动画。"
            val extra = if (i % 3 == 0) "本卡片用于展示瀑布流下不同高度的内容布局。" else ""
            Item(
                id = i.toLong(),
                title = title,
                subtitle = subtitleBase + extra,
                category = cat,
                color = color,
                likes = rnd.nextInt(900) + 12,
                liked = i % 5 == 0,
                createdAt = now - i * 60_000L,
                subtitleLines = 2 + (i % 3),
            )
        }
    }
}
