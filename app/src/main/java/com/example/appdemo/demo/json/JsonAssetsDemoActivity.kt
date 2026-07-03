package com.example.appdemo.demo.json

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 从 assets 目录读取 JSON —— 常见场景：
 *   · 城市列表 / 内置词典 / 默认配置
 *   · 首次启动 mock 一份数据落库
 *
 * 关键 API：
 *   context.assets.open(fileName)   → InputStream
 *   InputStream.bufferedReader().use { it.readText() }
 */
class JsonAssetsDemoActivity : AppCompatActivity() {

    private data class City(val code: String, val name: String, val province: String, val population: Int)

    private lateinit var log: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_json_assets)
        setupDemoToolbar(R.string.json_assets_title, R.id.json_assets_root)

        log = findViewById(R.id.tv_json_assets_log)

        findViewById<TextView>(R.id.btn_json_assets_read).setOnClickListener { readRaw() }
        findViewById<TextView>(R.id.btn_json_assets_parse).setOnClickListener { parse() }
        findViewById<TextView>(R.id.btn_json_assets_query).setOnClickListener { queryTop() }
        findViewById<TextView>(R.id.btn_json_assets_clear).setOnClickListener { log.text = getString(R.string.json_log_hint) }
    }

    /// 读原始文本
    private fun readRaw() {
        val text = loadAsset("cities.json")
        set("📁 原始文件内容（cities.json）", text)
    }

    /// 用 Gson 解析成 List<City>
    private fun parse() {
        val text = loadAsset("cities.json")
        val type = object : TypeToken<List<City>>() {}.type
        val cities: List<City> = Gson().fromJson(text, type)
        set(
            "📦 解析后 List<City>",
            "共 ${cities.size} 座城市",
            *cities.map { "· ${it.name}(${it.code}) · ${it.province} · ${it.population}w" }.toTypedArray()
        )
    }

    /// 综合案例：按人口降序取前 3
    private fun queryTop() {
        val text = loadAsset("cities.json")
        val cities: List<City> = Gson().fromJson(text, object : TypeToken<List<City>>() {}.type)
        val top3 = cities.sortedByDescending { it.population }.take(3)
        set(
            "🏆 人口前三",
            *top3.mapIndexed { i, c -> "${i + 1}. ${c.name} · ${c.population}w" }.toTypedArray(),
        )
    }

    /// 通用 asset 读取。use{} 自动关流。
    private fun loadAsset(name: String): String =
        assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private fun set(vararg lines: String) {
        log.text = lines.joinToString("\n")
    }
}
