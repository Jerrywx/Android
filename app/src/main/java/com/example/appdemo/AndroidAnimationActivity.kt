package com.example.appdemo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Android 动画总入口 —— 按类型列出全部动画 Demo。
 *
 * 涵盖的动画类型（12 种）：
 *   1. 补间动画（View Animation）：Alpha / Scale / Rotate / Translate / Set
 *   2. 属性动画（Property Animation）：ObjectAnimator / ValueAnimator / AnimatorSet
 *   3. 帧动画（Frame Animation）：AnimationDrawable 逐帧
 *   4. 插值器与求值器：Interpolator / TypeEvaluator
 *   5. 布局动画（LayoutAnimation）：容器入场
 *   6. ViewPropertyAnimator：链式调用
 *   7. 过渡动画（Transition）：场景切换
 *   8. 共享元素动画：Activity 间元素过渡
 *   9. Activity 转场动画：overridePendingTransition
 *   10. 物理动画（Physics-based）：SpringAnimation / FlingAnimation
 *   11. 矢量动画（AnimatedVector）：路径动画
 *   12. 自定义绘制动画：Canvas + ValueAnimator
 */
class AndroidAnimationActivity : AppCompatActivity() {

    private data class NavItem(val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_animation)
        setupDemoToolbar(R.string.anim_title, R.id.anim_root)

        val entries = buildEntries()

        val list = findViewById<RecyclerView>(R.id.anim_list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = EntryAdapter(entries) { entry ->
            startActivity(Intent(this, entry.target))
        }
    }

    private fun buildEntries(): List<NavItem> = listOf(
        NavItem(getString(R.string.anim_tween), getString(R.string.anim_tween_hint), TweenAnimationDemoActivity::class.java),
        NavItem(getString(R.string.anim_property), getString(R.string.anim_property_hint), PropertyAnimationDemoActivity::class.java),
        NavItem(getString(R.string.anim_frame), getString(R.string.anim_frame_hint), FrameAnimationDemoActivity::class.java),
        NavItem(getString(R.string.anim_interpolator), getString(R.string.anim_interpolator_hint), InterpolatorDemoActivity::class.java),
        NavItem(getString(R.string.anim_layout), getString(R.string.anim_layout_hint), LayoutAnimationDemoActivity::class.java),
        NavItem(getString(R.string.anim_view_property), getString(R.string.anim_view_property_hint), ViewPropertyAnimatorDemoActivity::class.java),
        NavItem(getString(R.string.anim_transition), getString(R.string.anim_transition_hint), TransitionDemoActivity::class.java),
        NavItem(getString(R.string.anim_shared_element), getString(R.string.anim_shared_element_hint), SharedElementDemoActivity::class.java),
        NavItem(getString(R.string.anim_activity_transition), getString(R.string.anim_activity_transition_hint), ActivityTransitionDemoActivity::class.java),
        NavItem(getString(R.string.anim_physics), getString(R.string.anim_physics_hint), PhysicsAnimationDemoActivity::class.java),
        NavItem(getString(R.string.anim_vector), getString(R.string.anim_vector_hint), VectorAnimationDemoActivity::class.java),
        NavItem(getString(R.string.anim_canvas), getString(R.string.anim_canvas_hint), CanvasAnimationDemoActivity::class.java),
    )

    private class EntryAdapter(
        private val items: List<NavItem>,
        private val onClick: (NavItem) -> Unit,
    ) : RecyclerView.Adapter<EntryAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_study_entry, parent, false)
            return VH(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) =
            holder.bind(items[position], position + 1, onClick)

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            private val card: View = view.findViewById(R.id.study_card)
            private val index: TextView = view.findViewById(R.id.study_index)
            private val title: TextView = view.findViewById(R.id.study_title)
            private val subtitle: TextView = view.findViewById(R.id.study_subtitle)

            fun bind(item: NavItem, no: Int, onClick: (NavItem) -> Unit) {
                index.text = no.toString()
                title.text = item.title
                subtitle.text = item.subtitle
                card.setOnClickListener { onClick(item) }
            }
        }
    }
}
