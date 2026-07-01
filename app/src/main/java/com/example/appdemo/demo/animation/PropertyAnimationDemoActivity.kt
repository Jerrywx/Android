package com.example.appdemo.demo.animation

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

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * 属性动画演示 —— ObjectAnimator / ValueAnimator / AnimatorSet。
 *
 * 与补间动画的区别：
 *   1) 真实修改 View 的属性，动画结束后状态保留
 *   2) 可以作用于任何对象的任意属性（不仅是 View）
 *   3) 支持颜色、自定义类型等任意值的插值
 */
class PropertyAnimationDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_property)
        setupDemoToolbar(R.string.anim_property, R.id.prop_root)

        val target = findViewById<View>(R.id.prop_target)
        val setTarget = findViewById<View>(R.id.set_target)

        /// 平移动画 —— translationX
        findViewById<TextView>(R.id.btn_translate).setOnClickListener {
            ObjectAnimator.ofFloat(target, "translationX", 0f, 300f, -300f, 0f).apply {
                duration = 1500
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }

        /// 旋转动画 —— rotationY（3D 翻转）
        findViewById<TextView>(R.id.btn_rotate).setOnClickListener {
            ObjectAnimator.ofFloat(target, "rotationY", 0f, 360f).apply {
                duration = 1200
                start()
            }
        }

        /// 缩放动画 —— PropertyValuesHolder 同时改 scaleX/scaleY
        findViewById<TextView>(R.id.btn_scale).setOnClickListener {
            val sx = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.6f, 1f)
            val sy = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.6f, 1f)
            ObjectAnimator.ofPropertyValuesHolder(target, sx, sy).apply {
                duration = 800
                interpolator = OvershootInterpolator()
                start()
            }
        }

        /// 透明度动画
        findViewById<TextView>(R.id.btn_alpha).setOnClickListener {
            ObjectAnimator.ofFloat(target, "alpha", 1f, 0.2f, 1f).apply {
                duration = 1200
                start()
            }
        }

        /// 颜色动画 —— ArgbEvaluator 自动插值颜色
        findViewById<TextView>(R.id.btn_color).setOnClickListener {
            val bg = target.background as? GradientDrawable ?: return@setOnClickListener
            ValueAnimator.ofObject(
                ArgbEvaluator(),
                Color.parseColor("#FF07C160"),
                Color.parseColor("#FFFF7E5F"),
                Color.parseColor("#FF6A8DFF"),
                Color.parseColor("#FF07C160"),
            ).apply {
                duration = 2000
                addUpdateListener { bg.setColor(it.animatedValue as Int) }
                start()
            }
        }

        /// 从 XML 加载属性动画
        findViewById<TextView>(R.id.btn_xml).setOnClickListener {
            val anim = AnimatorInflater.loadAnimator(this, R.animator.object_rotate_bounce)
            anim.setTarget(target)
            anim.start()
        }

        /// 多属性组合：旋转 + 缩放 + 平移
        findViewById<TextView>(R.id.btn_pvh).setOnClickListener {
            val pvhTx = PropertyValuesHolder.ofFloat("translationX", 0f, 200f, 0f)
            val pvhRot = PropertyValuesHolder.ofFloat("rotation", 0f, 720f)
            val pvhSx = PropertyValuesHolder.ofFloat("scaleX", 1f, 0.5f, 1f)
            val pvhSy = PropertyValuesHolder.ofFloat("scaleY", 1f, 0.5f, 1f)
            ObjectAnimator.ofPropertyValuesHolder(target, pvhTx, pvhRot, pvhSx, pvhSy).apply {
                duration = 1800
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }

        /// 重置
        findViewById<TextView>(R.id.btn_reset).setOnClickListener {
            target.animate().translationX(0f).translationY(0f)
                .scaleX(1f).scaleY(1f).rotation(0f).rotationY(0f)
                .alpha(1f).setDuration(300).start()
            (target.background as? GradientDrawable)?.setColor(Color.parseColor("#FF07C160"))
        }

        /// ValueAnimator 数字滚动
        val counter = findViewById<TextView>(R.id.tv_counter)
        findViewById<TextView>(R.id.btn_counter).setOnClickListener {
            ValueAnimator.ofInt(0, 8888).apply {
                duration = 2000
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { counter.text = (it.animatedValue as Int).toString() }
                start()
            }
        }

        /// AnimatorSet 同时播放
        findViewById<TextView>(R.id.btn_set_together).setOnClickListener {
            val rot = ObjectAnimator.ofFloat(setTarget, "rotation", 0f, 360f)
            val sx = ObjectAnimator.ofFloat(setTarget, "scaleX", 1f, 1.5f, 1f)
            val sy = ObjectAnimator.ofFloat(setTarget, "scaleY", 1f, 1.5f, 1f)
            AnimatorSet().apply {
                playTogether(rot, sx, sy)
                duration = 1200
                interpolator = OvershootInterpolator()
                start()
            }
        }

        /// AnimatorSet 顺序播放
        findViewById<TextView>(R.id.btn_set_sequence).setOnClickListener {
            val tx = ObjectAnimator.ofFloat(setTarget, "translationX", 0f, 200f).setDuration(500)
            val rot = ObjectAnimator.ofFloat(setTarget, "rotation", 0f, 360f).setDuration(800)
            val back = ObjectAnimator.ofFloat(setTarget, "translationX", 200f, 0f).setDuration(500)
            AnimatorSet().apply {
                playSequentially(tx, rot, back)
                start()
            }
        }
    }
}
