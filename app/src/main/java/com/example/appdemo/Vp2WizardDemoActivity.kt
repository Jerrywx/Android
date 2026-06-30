package com.example.appdemo

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class Vp2WizardDemoActivity : AppCompatActivity() {

    private val steps = listOf(
        R.layout.item_vp2_wiz_step1,
        R.layout.item_vp2_wiz_step2,
        R.layout.item_vp2_wiz_step3,
        R.layout.item_vp2_wiz_step4,
    )

    private lateinit var pager: ViewPager2
    private lateinit var indicator: LinearLayout
    private lateinit var stepText: TextView
    private lateinit var prev: TextView
    private lateinit var next: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vp2_wizard)
        setupDemoToolbar(R.string.vp2_wizard_title)

        pager = findViewById(R.id.vp2_wiz_pager)
        indicator = findViewById(R.id.vp2_wiz_indicator)
        stepText = findViewById(R.id.vp2_wiz_step_text)
        prev = findViewById(R.id.vp2_wiz_prev)
        next = findViewById(R.id.vp2_wiz_next)

        pager.adapter = WizardAdapter(steps)
        // 禁用左右滑动手势，只能通过底部按钮翻页
        pager.isUserInputEnabled = false
        pager.offscreenPageLimit = steps.size - 1

        // 让淡入淡出 + 轻微缩放更顺滑
        pager.setPageTransformer { page, position ->
            val abs = kotlin.math.abs(position)
            page.alpha = 1f - abs.coerceAtMost(1f) * 0.4f
            val scale = 1f - abs.coerceAtMost(1f) * 0.04f
            page.scaleX = scale
            page.scaleY = scale
        }

        buildIndicator()
        updateForPosition(0)

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updateForPosition(position)
        })

        prev.setOnClickListener {
            if (pager.currentItem > 0) pager.setCurrentItem(pager.currentItem - 1, true)
        }
        next.setOnClickListener {
            val cur = pager.currentItem
            if (cur < steps.lastIndex) {
                pager.setCurrentItem(cur + 1, true)
            } else {
                Toast.makeText(this, R.string.vp2_wizard_done, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateForPosition(position: Int) {
        stepText.text = getString(R.string.vp2_wizard_step, position + 1, steps.size)
        prev.alpha = if (position == 0) 0.4f else 1f
        prev.isEnabled = position != 0
        next.text = if (position == steps.lastIndex) getString(R.string.vp2_wizard_finish)
        else getString(R.string.vp2_wizard_next)
        updateIndicator(position)
    }

    private fun buildIndicator() {
        indicator.removeAllViews()
        val d = resources.displayMetrics.density
        val size = (28 * d).toInt()
        steps.forEachIndexed { i, _ ->
            val dot = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size)
                gravity = android.view.Gravity.CENTER
                textSize = 13f
                text = (i + 1).toString()
                setTextColor(0xFFFFFFFF.toInt())
                background = circle(0xFFD0D5DC.toInt())
            }
            indicator.addView(dot)
            if (i != steps.lastIndex) {
                val line = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, (2 * d).toInt(), 1f)
                    setBackgroundColor(0xFFD0D5DC.toInt())
                }
                indicator.addView(line)
            }
        }
    }

    private fun updateIndicator(active: Int) {
        var child = 0
        val activeColor = 0xFF07C160.toInt()
        val pendingColor = 0xFFD0D5DC.toInt()
        for (i in steps.indices) {
            val dot = indicator.getChildAt(child++) as TextView
            val on = i <= active
            dot.background = circle(if (on) activeColor else pendingColor)
            dot.animate().cancel()
            dot.animate().scaleX(if (i == active) 1.15f else 1f)
                .scaleY(if (i == active) 1.15f else 1f).setDuration(200).start()
            if (i != steps.lastIndex) {
                val line = indicator.getChildAt(child++)
                line.setBackgroundColor(if (i < active) activeColor else pendingColor)
            }
        }
    }

    private fun circle(color: Int): GradientDrawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.OVAL
        d.setColor(color)
        return d
    }

    private class WizardAdapter(val steps: List<Int>) : RecyclerView.Adapter<WizardAdapter.VH>() {
        override fun getItemCount(): Int = steps.size
        override fun getItemViewType(position: Int): Int = steps[position]
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            v.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            return VH(v)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            when (position) {
                1 -> bindStep2(holder.itemView)
                2 -> bindStep3(holder.itemView)
            }
        }

        private fun bindStep2(v: View) {
            v.findViewById<TextView>(R.id.vp2_wiz2_title).setText(R.string.vp2_wizard_step2_title)
            v.findViewById<TextView>(R.id.vp2_wiz2_sub).setText(R.string.vp2_wizard_step2_sub)
            val group = v.findViewById<ChipGroup>(R.id.vp2_wiz2_chips)
            if (group.childCount > 0) return
            val tags = listOf("Compose", "Kotlin", "协程", "动画", "性能", "Material 3", "测试", "架构", "Navigation")
            tags.forEach { t ->
                val chip = Chip(v.context).apply {
                    text = t
                    isCheckable = true
                    isClickable = true
                }
                group.addView(chip)
            }
        }

        private fun bindStep3(v: View) {
            v.findViewById<TextView>(R.id.vp2_wiz3_title).setText(R.string.vp2_wizard_step3_title)
            v.findViewById<TextView>(R.id.vp2_wiz3_sub).setText(R.string.vp2_wizard_step3_sub)
            // 让 Switch 默认就有可见状态
            v.findViewById<SwitchCompat>(R.id.vp2_wiz3_switch_msg)
            v.findViewById<SwitchCompat>(R.id.vp2_wiz3_switch_sound)
            v.findViewById<SwitchCompat>(R.id.vp2_wiz3_switch_vibrate)
        }

        class VH(view: View) : RecyclerView.ViewHolder(view)
    }
}
