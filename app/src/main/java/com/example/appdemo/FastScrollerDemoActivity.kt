package com.example.appdemo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FastScrollerDemoActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var bubble: TextView
    private lateinit var indexBar: AlphabetIndexBar
    private val rows = FastScrollerData.rows()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo_fast)
        setupDemoToolbar(R.string.fast_scroller_title)

        list = findViewById(R.id.fast_list)
        bubble = findViewById(R.id.fast_bubble)
        indexBar = findViewById(R.id.fast_index_bar)

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = FastAdapter(rows)

        val letters = rows.mapNotNull { (it as? FastRow.Header)?.letter }.distinct()
        indexBar.setLetters(letters)
        indexBar.onLetterChanged = { letter, pressing ->
            bubble.text = letter
            bubble.visibility = if (pressing) View.VISIBLE else View.INVISIBLE
            val pos = rows.indexOfFirst { it is FastRow.Header && it.letter == letter }
            if (pos >= 0) (list.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(pos, 0)
        }
    }
}

sealed class FastRow {
    data class Header(val letter: String) : FastRow()
    data class Contact(val name: String, val pinyin: String, val color: Int) : FastRow()
}

object FastScrollerData {
    fun rows(): List<FastRow> {
        val raw = listOf(
            "Alice" to "Alice 创意工作室",
            "Aaron" to "Aaron 团队",
            "Ben" to "Ben 工作伙伴",
            "Bella" to "Bella 设计师",
            "Cathy" to "Cathy 客户经理",
            "Carl" to "Carl 项目经理",
            "Daniel" to "Daniel 后端工程师",
            "David" to "David 同事",
            "Emma" to "Emma 产品",
            "Eric" to "Eric 同学",
            "Fanny" to "Fanny 设计",
            "Frank" to "Frank 前端",
            "Grace" to "Grace 朋友",
            "Henry" to "Henry 测试",
            "Iris" to "Iris 同事",
            "Jack" to "Jack 老板",
            "Jane" to "Jane 顾问",
            "Kevin" to "Kevin 同学",
            "Lucy" to "Lucy 产品",
            "Mike" to "Mike 朋友",
            "Nick" to "Nick 客户",
            "Oscar" to "Oscar 合作伙伴",
            "Peter" to "Peter 同事",
            "Queenie" to "Queenie 同学",
            "Rita" to "Rita 同事",
            "Sam" to "Sam 老乡",
            "Tom" to "Tom 同学",
            "Uma" to "Uma 朋友",
            "Victor" to "Victor 顾问",
            "Wendy" to "Wendy 客户",
            "Xavier" to "Xavier 同事",
            "Yvonne" to "Yvonne 同学",
            "Zoe" to "Zoe 朋友",
        )
        val palette = intArrayOf(
            0xFF6A8DFF.toInt(), 0xFFFF8A65.toInt(), 0xFF22C1C3.toInt(),
            0xFFFFB94B.toInt(), 0xFF8E66FF.toInt(), 0xFFFF5E8C.toInt(),
            0xFF3CCB8A.toInt(), 0xFF07C160.toInt(),
        )
        val sorted = raw.sortedBy { it.first }
        val rows = mutableListOf<FastRow>()
        var current = ""
        sorted.forEachIndexed { i, (name, pin) ->
            val letter = name.first().uppercaseChar().toString()
            if (letter != current) {
                rows += FastRow.Header(letter)
                current = letter
            }
            rows += FastRow.Contact(name, pin, palette[i % palette.size])
        }
        return rows
    }
}

private class FastAdapter(val rows: List<FastRow>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemViewType(position: Int): Int = if (rows[position] is FastRow.Header) 0 else 1
    override fun getItemCount(): Int = rows.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == 0) HeaderVH(inf.inflate(R.layout.item_demo_fast_header, parent, false))
        else ContactVH(inf.inflate(R.layout.item_demo_fast_contact, parent, false))
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val r = rows[position]) {
            is FastRow.Header -> (holder.itemView as TextView).text = r.letter
            is FastRow.Contact -> (holder as ContactVH).bind(r)
        }
    }

    class HeaderVH(view: View) : RecyclerView.ViewHolder(view)
    class ContactVH(view: View) : RecyclerView.ViewHolder(view) {
        private val avatar: TextView = view.findViewById(R.id.fast_avatar)
        private val name: TextView = view.findViewById(R.id.fast_name)
        private val subtitle: TextView = view.findViewById(R.id.fast_subtitle)
        fun bind(c: FastRow.Contact) {
            avatar.text = c.name.first().toString()
            val d = GradientDrawable()
            d.shape = GradientDrawable.OVAL
            d.setColor(c.color)
            avatar.background = d
            name.text = c.name
            subtitle.text = c.pinyin
        }
    }
}

class AlphabetIndexBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    var onLetterChanged: ((letter: String, pressing: Boolean) -> Unit)? = null

    private var letters: List<String> = emptyList()
    private var pressing = false
    private var current = -1

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.demo_fastscroll_index)
        textAlign = Paint.Align.CENTER
        textSize = 11f * resources.displayMetrics.density
    }

    fun setLetters(list: List<String>) {
        letters = list
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (letters.isEmpty()) return
        val cellH = height.toFloat() / letters.size
        val cx = width / 2f
        letters.forEachIndexed { i, l ->
            paint.color = if (i == current) context.getColor(R.color.demo_accent)
            else context.getColor(R.color.demo_fastscroll_index)
            paint.isFakeBoldText = i == current
            val y = cellH * i + cellH / 2f - (paint.ascent() + paint.descent()) / 2f
            canvas.drawText(l, cx, y, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (letters.isEmpty()) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                pressing = true
                val idx = (event.y / (height.toFloat() / letters.size)).toInt().coerceIn(0, letters.size - 1)
                if (idx != current) {
                    current = idx
                    invalidate()
                    onLetterChanged?.invoke(letters[idx], true)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                pressing = false
                val last = current.takeIf { it in letters.indices } ?: return true
                onLetterChanged?.invoke(letters[last], false)
                current = -1
                invalidate()
            }
        }
        return true
    }
}
