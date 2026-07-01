package com.example.appdemo.demo.storage

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/// 通过属性委托创建单例 DataStore，全应用共享
private val Context.demoDataStore by preferencesDataStore(name = "demo_datastore")

/**
 * DataStore（Preferences）演示 ——
 *
 * 相比 SharedPreferences 的优势：
 *   • 基于 Flow，读写异步、支持事务
 *   • 通过 Kotlin 协程调用，不会阻塞主线程
 *   • 强类型 Key（Preferences.Key<T>），编译期检查
 *
 * 本示例演示：
 *   1) 保存字符串 / 整数 / 布尔
 *   2) 使用 Flow 实时观察值的变化
 *   3) 一次 edit 内多字段原子写入
 */
class DataStoreDemoActivity : AppCompatActivity() {

    private companion object {
        val KEY_NICK = stringPreferencesKey("user_nick")
        val KEY_LEVEL = intPreferencesKey("user_level")
        val KEY_DARK = booleanPreferencesKey("dark_mode")
    }

    private lateinit var log: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_storage_datastore)
        setupDemoToolbar(R.string.storage_datastore_title, R.id.storage_datastore_root)

        log = findViewById(R.id.tv_ds_log)
        val nickInput = findViewById<EditText>(R.id.et_ds_nick)
        val levelInput = findViewById<EditText>(R.id.et_ds_level)
        val flowText = findViewById<TextView>(R.id.tv_ds_flow)

        /// 使用 Flow 实时观察 nick 的变化 —— UI 会自动响应任何写入
        lifecycleScope.launch {
            demoDataStore.data
                .map { it[KEY_NICK] to it[KEY_LEVEL] }
                .collect { (nick, level) ->
                    flowText.text = getString(R.string.storage_ds_flow_value, nick ?: "—", level ?: -1)
                }
        }

        findViewById<TextView>(R.id.btn_ds_save).setOnClickListener {
            val nick = nickInput.text.toString()
            val level = levelInput.text.toString().toIntOrNull() ?: 0
            lifecycleScope.launch {
                /// edit 是原子操作，多个字段要么都写入要么都不写
                demoDataStore.edit { prefs ->
                    prefs[KEY_NICK] = nick
                    prefs[KEY_LEVEL] = level
                }
                appendLog("✅ 已保存：nick=$nick, level=$level")
            }
        }

        findViewById<TextView>(R.id.btn_ds_toggle).setOnClickListener {
            lifecycleScope.launch {
                demoDataStore.edit { prefs ->
                    val old = prefs[KEY_DARK] ?: false
                    prefs[KEY_DARK] = !old
                    appendLog("🌗 darkMode: $old → ${!old}")
                }
            }
        }

        findViewById<TextView>(R.id.btn_ds_read).setOnClickListener {
            /// first() 单次取当前快照，不订阅后续变化
            lifecycleScope.launch {
                val snapshot = demoDataStore.data.first()
                val nick = snapshot[KEY_NICK] ?: "—"
                val level = snapshot[KEY_LEVEL] ?: -1
                val dark = snapshot[KEY_DARK] ?: false
                appendLog("📖 快照：nick=$nick, level=$level, dark=$dark")
            }
        }

        findViewById<TextView>(R.id.btn_ds_clear).setOnClickListener {
            lifecycleScope.launch {
                demoDataStore.edit { it.clear() }
                appendLog("🗑️ 已清空全部键值")
            }
        }
    }

    private fun appendLog(msg: String) {
        log.text = if (log.text.isNullOrBlank() || log.text.startsWith("点击")) msg
        else "${log.text}\n$msg"
    }
}
