package com.example.appdemo.demo.customview

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.customview.widget.WaveView

/**
 * 贝塞尔曲线波浪 —— Path + 二阶贝塞尔实战。
 *
 * 涵盖：
 *   1) Path.quadTo 拼接波峰波谷
 *   2) ValueAnimator 无限循环驱动横向位移
 *   3) 双层波浪 + 半透明叠加做层次
 *   4) SeekBar 实时调水位
 */
class WaveDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customview_wave)
        setupDemoToolbar(R.string.customview_wave_title, R.id.customview_wave_root)

        val wave = findViewById<WaveView>(R.id.customview_wave_view)
        val seek = findViewById<SeekBar>(R.id.customview_wave_seek)

        wave.setProgress(60)
        seek.progress = 60
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                wave.setProgress(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        findViewById<Button>(R.id.customview_wave_random).setOnClickListener {
            val target = (10..95).random()
            seek.progress = target
            wave.setProgress(target)
        }
    }
}
