package com.example.appdemo

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * 矢量动画演示 —— AnimatedVectorDrawable。
 *
 * 组成：
 *   1) vector  —— 描述矢量形状（pathData）
 *   2) animated-vector —— 把目标 path 绑定到属性动画
 *   3) animator —— 真正的动画定义，可对 pathData、fillAlpha 等属性做动画
 */
class VectorAnimationDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_vector)
        setupDemoToolbar(R.string.anim_vector, R.id.vec_root)

        /// 播放/暂停形态变换
        val iconPlay = findViewById<ImageView>(R.id.iv_play_pause)
        findViewById<TextView>(R.id.btn_play_pause).setOnClickListener {
            (iconPlay.drawable as? AnimatedVectorDrawable)?.apply {
                stop()
                start()
            }
        }

        /// 心跳动画
        val iconHeart = findViewById<ImageView>(R.id.iv_heart)
        findViewById<TextView>(R.id.btn_heart).setOnClickListener {
            (iconHeart.drawable as? AnimatedVectorDrawable)?.apply {
                stop()
                start()
            }
        }
    }
}
