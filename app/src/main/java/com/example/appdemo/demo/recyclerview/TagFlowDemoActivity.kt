package com.example.appdemo.demo.recyclerview

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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class TagFlowDemoActivity : AppCompatActivity() {

    private val tags = TagFlowData.tags().toMutableList()
    private val selected = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo_tag)
        setupDemoToolbar(R.string.tag_flow_title)

        val list = findViewById<RecyclerView>(R.id.tag_list)
        list.layoutManager = FlowLayoutManager()
        val adapter = TagAdapter(tags, isSelected = { selected.contains(it) }) { tag, pos ->
            if (selected.contains(tag)) selected.remove(tag) else selected.add(tag)
            list.adapter?.notifyItemChanged(pos, "sel")
        }
        list.adapter = adapter
        list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply { changeDuration = 160 }
    }
}

object TagFlowData {
    fun tags(): List<String> = listOf(
        "Kotlin", "Coroutines", "Flow", "RecyclerView", "Compose", "Jetpack",
        "Room", "Navigation", "ViewModel", "LiveData", "WorkManager", "Paging 3",
        "Material 3", "MotionLayout", "性能优化", "启动优化", "包大小", "稳定性",
        "Hilt", "Dagger", "DataStore", "DataBinding", "ViewBinding", "Glide",
        "Coil", "OkHttp", "Retrofit", "Moshi", "Gson", "Espresso", "Compose 测试",
        "Profiler", "LeakCanary", "WindowInsets", "Edge-to-Edge", "Accessibility",
    )
}

private class TagAdapter(
    val items: List<String>,
    val isSelected: (String) -> Boolean,
    val onClick: (String, Int) -> Unit,
) : RecyclerView.Adapter<TagAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_demo_tag, parent, false)
        return VH(v as TextView)
    }
    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position], isSelected(items[position]), position, onClick)
    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
        else holder.bindSelection(isSelected(items[position]), animate = true)
    }

    class VH(val view: TextView) : RecyclerView.ViewHolder(view) {
        fun bind(text: String, sel: Boolean, pos: Int, onClick: (String, Int) -> Unit) {
            view.text = text
            bindSelection(sel, animate = false)
            view.setOnClickListener { onClick(text, pos) }
        }
        fun bindSelection(sel: Boolean, animate: Boolean) {
            view.isSelected = sel
            if (animate) {
                view.animate().cancel()
                view.scaleX = 0.85f; view.scaleY = 0.85f
                view.animate().scaleX(1f).scaleY(1f).setDuration(180).start()
            }
        }
    }
}

private class FlowLayoutManager : RecyclerView.LayoutManager() {
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
        RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )

    private var verticalOffset = 0
    private var totalHeight = 0

    override fun canScrollVertically(): Boolean = true

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }
        detachAndScrapAttachedViews(recycler)
        fill(recycler)
    }

    private fun fill(recycler: RecyclerView.Recycler) {
        val density = (this.width.takeIf { it > 0 } ?: 1).let { 1f }
        val horizontalPadding = 4
        val verticalPadding = 4
        var x = paddingLeft
        var y = paddingTop - verticalOffset
        var lineHeight = 0
        totalHeight = paddingTop
        for (i in 0 until itemCount) {
            val view = recycler.getViewForPosition(i)
            addView(view)
            measureChildWithMargins(view, 0, 0)
            val w = getDecoratedMeasuredWidth(view)
            val h = getDecoratedMeasuredHeight(view)
            if (x + w > width - paddingRight) {
                x = paddingLeft
                y += lineHeight + verticalPadding
                totalHeight += lineHeight + verticalPadding
                lineHeight = 0
            }
            layoutDecorated(view, x, y, x + w, y + h)
            x += w + horizontalPadding
            if (h > lineHeight) lineHeight = h
        }
        totalHeight += lineHeight + paddingBottom
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (childCount == 0 || dy == 0) return 0
        val newOffset = (verticalOffset + dy).coerceAtLeast(0)
            .coerceAtMost((totalHeight - height).coerceAtLeast(0))
        val consumed = newOffset - verticalOffset
        verticalOffset = newOffset
        offsetChildrenVertical(-consumed)
        return consumed
    }
}
