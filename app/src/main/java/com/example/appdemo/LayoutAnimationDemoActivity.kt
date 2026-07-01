package com.example.appdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * LayoutAnimation 演示 —— 为容器内每个子 View 应用入场动画。
 *
 * 核心步骤：
 *   1) 在 res/anim/ 下定义单个 item 的入场动画
 *   2) 定义 layoutAnimation 引用该动画，并配置 delay/order
 *   3) 给容器设置 layoutAnimation 后立即 scheduleLayoutAnimation()
 */
class LayoutAnimationDemoActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_layout)
        setupDemoToolbar(R.string.anim_layout, R.id.layoutanim_root)

        list = findViewById(R.id.layoutanim_list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = SimpleAdapter((1..20).map { "条目 #$it · 优雅滑入" })

        /// 默认播放一次「掉落」动画
        playFall()

        findViewById<TextView>(R.id.btn_fall).setOnClickListener { playFall() }
        findViewById<TextView>(R.id.btn_right).setOnClickListener { playRight() }
    }

    private fun playFall() {
        list.layoutAnimation = AnimationUtils
            .loadLayoutAnimation(this, R.anim.layout_animation_fall_down)
        list.adapter?.notifyDataSetChanged()
        list.scheduleLayoutAnimation()
    }

    private fun playRight() {
        list.layoutAnimation = AnimationUtils
            .loadLayoutAnimation(this, R.anim.layout_animation_right_in)
        list.adapter?.notifyDataSetChanged()
        list.scheduleLayoutAnimation()
    }

    private class SimpleAdapter(private val data: List<String>) :
        RecyclerView.Adapter<SimpleAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_study_entry, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(data[position], position + 1)
        }

        override fun getItemCount(): Int = data.size

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            private val index: TextView = view.findViewById(R.id.study_index)
            private val title: TextView = view.findViewById(R.id.study_title)
            private val subtitle: TextView = view.findViewById(R.id.study_subtitle)
            fun bind(text: String, no: Int) {
                index.text = no.toString()
                title.text = text
                subtitle.text = "LayoutAnimation Demo"
            }
        }
    }
}
