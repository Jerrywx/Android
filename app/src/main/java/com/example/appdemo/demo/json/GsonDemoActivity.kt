package com.example.appdemo.demo.json

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

/**
 * Gson 演示 —— 项目已经通过 retrofit-converter-gson 间接引入。
 *
 *   · 数据类 <-> JSON 字符串
 *   · @SerializedName 处理字段名不一致
 *   · TypeToken 解析泛型 List / Map
 *   · GsonBuilder 常用配置：美化输出、日期格式、忽略 null
 */
class GsonDemoActivity : AppCompatActivity() {

    /// 数据类：Kotlin data class 直接映射 JSON
    /// @SerializedName 让 JSON 里的 user_id 映射到 Kotlin 的 userId
    private data class User(
        @SerializedName("user_id") val userId: Long,
        val name: String,
        val age: Int? = null,     /// null 默认值，缺字段时用它
        val tags: List<String> = emptyList(),
    )

    private lateinit var log: TextView
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_json_gson)
        setupDemoToolbar(R.string.json_gson_title, R.id.json_gson_root)

        log = findViewById(R.id.tv_json_gson_log)

        findViewById<TextView>(R.id.btn_gson_to_json).setOnClickListener { toJson() }
        findViewById<TextView>(R.id.btn_gson_from_json).setOnClickListener { fromJson() }
        findViewById<TextView>(R.id.btn_gson_list).setOnClickListener { parseList() }
        findViewById<TextView>(R.id.btn_gson_map).setOnClickListener { parseMap() }
        findViewById<TextView>(R.id.btn_gson_pretty).setOnClickListener { pretty() }
        findViewById<TextView>(R.id.btn_gson_clear).setOnClickListener { log.text = getString(R.string.json_log_hint) }
    }

    /// 对象 -> JSON 字符串
    private fun toJson() {
        val user = User(
            userId = 1024,
            name = "王小明",
            age = 28,
            tags = listOf("android", "kotlin")
        )
        val json = gson.toJson(user)
        set("📤 toJson(user)", json)
    }

    /// JSON 字符串 -> 对象
    private fun fromJson() {
        val src = """{"user_id":2048,"name":"李三","age":22,"tags":["compose","flow"]}"""
        val user = gson.fromJson(src, User::class.java)
        set(
            "📥 fromJson",
            "输入：$src",
            "解析后：$user",
            "userId=${user.userId}, tags=${user.tags}",
        )
    }

    /// 泛型 List<User>：TypeToken 保留类型信息，避免类型擦除
    private fun parseList() {
        val src = """
            [
              {"user_id":1,"name":"A","tags":["x","y"]},
              {"user_id":2,"name":"B","age":19,"tags":[]}
            ]
        """.trimIndent()

        val listType = object : TypeToken<List<User>>() {}.type
        val list: List<User> = gson.fromJson(src, listType)

        set(
            "📚 泛型 List<User>",
            "共 ${list.size} 条：",
            *list.map { "· $it" }.toTypedArray()
        )
    }

    /// 泛型 Map<String, Any>：Any 会退化为 Double / Boolean / Map / List
    private fun parseMap() {
        val src = """{"code":0,"ok":true,"meta":{"page":1,"size":20}}"""
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val map: Map<String, Any> = gson.fromJson(src, mapType)

        set(
            "🗺️ 泛型 Map",
            *map.map { "· ${it.key} = ${it.value}  (${it.value::class.java.simpleName})" }.toTypedArray()
        )
    }

    /// GsonBuilder 常用配置
    private fun pretty() {
        val user = User(1, "prettyGson", 30, listOf("好看", "美化"))

        val pretty = GsonBuilder()
            .setPrettyPrinting()      /// 美化输出，带换行 / 缩进
            .serializeNulls()          /// 保留 null 字段
            .create()

        set(
            "🎨 GsonBuilder 美化",
            pretty.toJson(user),
        )
    }

    private fun set(vararg lines: String) {
        log.text = lines.joinToString("\n")
    }
}
