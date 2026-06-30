package com.example.appdemo

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class Vp2OnboardingDemoActivity : AppCompatActivity() {

    private data class Page(val title: String, val sub: String, val start: Int, val end: Int, val emoji: String)

    private val pages by lazy {
        listOf(
            Page(getString(R.string.vp2_onboarding_page1_title), getString(R.string.vp2_onboarding_page1_sub), 0xFF6A8DFF.toInt(), 0xFF8E66FF.toInt(), "📋"),
            Page(getString(R.string.vp2_onboarding_page2_title), getString(R.string.vp2_onboarding_page2_sub), 0xFFFF8A65.toInt(), 0xFFFF5E8C.toInt(), "🎞"),
            Page(getString(R.string.vp2_onboarding_page3_title), getString(R.string.vp2_onboarding_page3_sub), 0xFF22C1C3.toInt(), 0xFF3CCB8A.toInt(), "🧩"),
            Page(getString(R.string.vp2_onboarding_page4_title), getString(R.string.vp2_onboarding_page4_sub), 0xFFFFB94B.toInt(), 0xFFFF7E5F.toInt(), "🚀"),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vp2_onboarding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.vp2_onb_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val pager = findViewById<ViewPager2>(R.id.vp2_onb_pager)
        val dots = findViewById<LinearLayout>(R.id.vp2_onb_dots)
        val skip = findViewById<TextView>(R.id.vp2_onb_skip)
        val next = findViewById<TextView>(R.id.vp2_onb_next)

        pager.adapter = OnboardingAdapter(pages)
        pager.setPageTransformer { page, position ->
            val emoji = page.findViewById<TextView>(R.id.vp2_onb_emoji)
            val title = page.findViewById<TextView>(R.id.vp2_onb_title)
            val sub = page.findViewById<TextView>(R.id.vp2_onb_sub)
            val abs = kotlin.math.abs(position)
            emoji.translationX = -position * page.width * 0.35f
            title.translationX = -position * page.width * 0.22f
            sub.translationX = -position * page.width * 0.12f
            page.alpha = 1f - abs.coerceAtMost(1f) * 0.4f
        }

        buildDots(dots)
        updateDots(dots, 0)
        updateNext(next, 0)

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(dots, position)
                updateNext(next, position)
            }
        })

        skip.setOnClickListener { finish() }
        next.setOnClickListener {
            val current = pager.currentItem
            if (current < pages.lastIndex) {
                pager.setCurrentItem(current + 1, true)
            } else {
                Toast.makeText(this, R.string.vp2_onboarding_start, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateNext(btn: TextView, position: Int) {
        btn.text = if (position == pages.lastIndex) getString(R.string.vp2_onboarding_start)
        else getString(R.string.vp2_onboarding_next)
    }

    private fun buildDots(parent: LinearLayout) {
        parent.removeAllViews()
        val d = resources.displayMetrics.density
        pages.forEachIndexed { i, _ ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams((8 * d).toInt(), (8 * d).toInt()).apply {
                    if (i != 0) marginStart = (8 * d).toInt()
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(0x66FFFFFF)
                }
            }
            parent.addView(dot)
        }
    }

    private fun updateDots(parent: LinearLayout, selected: Int) {
        val d = resources.displayMetrics.density
        for (i in 0 until parent.childCount) {
            val dot = parent.getChildAt(i)
            val on = i == selected
            dot.animate().cancel()
            dot.animate()
                .scaleX(if (on) 2.4f else 1f)
                .scaleY(if (on) 1f else 1f)
                .alpha(if (on) 1f else 0.6f)
                .setDuration(220).start()
        }
    }

    private class OnboardingAdapter(val pages: List<Page>) : RecyclerView.Adapter<OnboardingAdapter.VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vp2_onboarding, parent, false)
            return VH(v)
        }
        override fun getItemCount(): Int = pages.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(pages[position])

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            private val root: FrameLayout = v.findViewById(R.id.vp2_onb_item_root)
            private val emoji: TextView = v.findViewById(R.id.vp2_onb_emoji)
            private val title: TextView = v.findViewById(R.id.vp2_onb_title)
            private val sub: TextView = v.findViewById(R.id.vp2_onb_sub)
            fun bind(p: Page) {
                emoji.text = p.emoji
                title.text = p.title
                sub.text = p.sub
                val d = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(p.start, p.end))
                root.background = d
            }
        }
    }
}
