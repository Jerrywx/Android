package com.example.appdemo.demo.customview

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.customview.widget.StarRatingView

/**
 * 星评分控件 —— Path + clipRect 支持半星，手指拖动打分。
 *
 * 涵盖：
 *   1) 用 Path 缓存五角星形状，绘制时按索引平移复用
 *   2) clipRect 裁剪出半星填充比例
 *   3) 手指按下 / 拖动换算 rating，抬手对齐到最近半星
 *   4) ValueAnimator 平滑到目标值
 */
class StarRatingDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customview_star_rating)
        setupDemoToolbar(R.string.customview_rating_title, R.id.customview_rating_root)

        val star = findViewById<StarRatingView>(R.id.customview_rating_view)
        val label = findViewById<TextView>(R.id.customview_rating_label)

        fun renderLabel(value: Float) {
            label.text = getString(R.string.customview_rating_current, value)
        }

        star.setOnRatingChanged { renderLabel(it) }
        star.setRating(3.5f)
        renderLabel(star.rating)

        findViewById<Button>(R.id.customview_rating_random).setOnClickListener {
            val next = ((0..10).random()) / 2f
            star.setRating(next, animate = true)
        }
    }
}
