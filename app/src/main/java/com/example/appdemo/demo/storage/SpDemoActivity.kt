package com.example.appdemo.demo.storage

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar

/**
 * SharedPreferences 演示 ——
 *   1) 通过 getSharedPreferences 拿到实例
 *   2) edit() 修改，apply() 异步提交，commit() 同步提交
 *   3) OnSharedPreferenceChangeListener 监听变化
 *   4) 保存 String / Int / Boolean 三种常见类型
 */
class SpDemoActivity : AppCompatActivity() {

    private companion object {
        const val PREF_NAME = "demo_sp"
        const val KEY_NICK = "user_nick"
        const val KEY_LEVEL = "user_level"
        const val KEY_PUSH = "push_enabled"
    }

    private lateinit var sp: SharedPreferences
    private lateinit var log: TextView

    /// 变化监听器：任何字段修改都会回调
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        appendLog("🔔 检测到变化：key=$key")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_storage_sp)
        setupDemoToolbar(R.string.storage_sp_title, R.id.storage_sp_root)

        sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.registerOnSharedPreferenceChangeListener(listener)

        log = findViewById(R.id.tv_sp_log)

        val nickInput = findViewById<EditText>(R.id.et_sp_nick)
        val levelBar = findViewById<SeekBar>(R.id.sb_sp_level)
        val pushBox = findViewById<CheckBox>(R.id.cb_sp_push)
        val levelText = findViewById<TextView>(R.id.tv_sp_level)

        /// 读出上次保存值填回 UI
        nickInput.setText(sp.getString(KEY_NICK, ""))
        val savedLevel = sp.getInt(KEY_LEVEL, 1)
        levelBar.progress = savedLevel
        levelText.text = getString(R.string.storage_sp_level_value, savedLevel)
        pushBox.isChecked = sp.getBoolean(KEY_PUSH, true)

        levelBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                levelText.text = getString(R.string.storage_sp_level_value, progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) = Unit
            override fun onStopTrackingTouch(sb: SeekBar?) = Unit
        })

        findViewById<TextView>(R.id.btn_sp_apply).setOnClickListener {
            /// apply() 异步写盘，返回 void，性能好
            sp.edit()
                .putString(KEY_NICK, nickInput.text.toString())
                .putInt(KEY_LEVEL, levelBar.progress)
                .putBoolean(KEY_PUSH, pushBox.isChecked)
                .apply()
            appendLog("✅ apply() 异步保存完成")
        }

        findViewById<TextView>(R.id.btn_sp_commit).setOnClickListener {
            /// commit() 同步写盘，返回 boolean，需注意主线程 IO
            val ok = sp.edit()
                .putString(KEY_NICK, nickInput.text.toString())
                .putInt(KEY_LEVEL, levelBar.progress)
                .putBoolean(KEY_PUSH, pushBox.isChecked)
                .commit()
            appendLog("✅ commit() 同步保存返回：$ok")
        }

        findViewById<TextView>(R.id.btn_sp_read).setOnClickListener {
            val nick = sp.getString(KEY_NICK, "—")
            val level = sp.getInt(KEY_LEVEL, 1)
            val push = sp.getBoolean(KEY_PUSH, false)
            appendLog("📖 读取：nick=$nick, level=$level, push=$push")
        }

        findViewById<TextView>(R.id.btn_sp_clear).setOnClickListener {
            sp.edit().clear().apply()
            nickInput.setText("")
            levelBar.progress = 0
            pushBox.isChecked = false
            appendLog("🗑️ 已清空全部键值")
        }
    }

    private fun appendLog(msg: String) {
        log.text = if (log.text.isNullOrBlank() || log.text.startsWith("点击")) msg
        else "${log.text}\n$msg"
    }

    override fun onDestroy() {
        sp.unregisterOnSharedPreferenceChangeListener(listener)
        super.onDestroy()
    }
}
