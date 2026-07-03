package com.example.appdemo.demo.json

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import org.json.JSONArray
import org.json.JSONObject

/**
 * JSONObject / JSONArray 演示 —— Android 系统内置，无需三方依赖。
 *
 *   · 构造：put 链式；opt* 系列取值带默认值，安全
 *   · 解析：getX 会抛异常，optX 拿不到时返回默认
 *   · 遍历：JSONObject.keys() / JSONArray.length()
 *   · 序列化：toString(indent) 可以带缩进美化
 */
class JsonObjectDemoActivity : AppCompatActivity() {

    private lateinit var log: TextView

    private val sampleJson = """
        {
          "code": 0,
          "msg": "ok",
          "data": {
            "id": 1024,
            "name": "王小明",
            "vip": true,
            "tags": ["android", "kotlin", "compose"]
          }
        }
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_json_object)
        setupDemoToolbar(R.string.json_object_title, R.id.json_object_root)

        log = findViewById(R.id.tv_json_object_log)
        findViewById<TextView>(R.id.tv_json_object_sample).text = sampleJson

        findViewById<TextView>(R.id.btn_json_object_build).setOnClickListener { build() }
        findViewById<TextView>(R.id.btn_json_object_parse).setOnClickListener { parse() }
        findViewById<TextView>(R.id.btn_json_object_iterate).setOnClickListener { iterate() }
        findViewById<TextView>(R.id.btn_json_object_safe).setOnClickListener { safeAccess() }
        findViewById<TextView>(R.id.btn_json_object_clear).setOnClickListener { log.text = getString(R.string.json_log_hint) }
    }

    /// 构造 JSON —— put 支持链式，null 值会被存为 JSONObject.NULL
    private fun build() {
        val tags = JSONArray().apply {
            put("android"); put("kotlin"); put("compose")
        }

        val data = JSONObject()
            .put("id", 1024)
            .put("name", "王小明")
            .put("vip", true)
            .put("tags", tags)

        val root = JSONObject()
            .put("code", 0)
            .put("msg", "ok")
            .put("data", data)

        /// toString(2) —— 缩进 2 个空格，美化输出
        set("🔨 构造 JSON\n${root.toString(2)}")
    }

    /// 解析 —— getX 系列，key 不存在会抛 JSONException
    private fun parse() {
        val root = JSONObject(sampleJson)
        val code = root.getInt("code")
        val msg = root.getString("msg")

        val data = root.getJSONObject("data")
        val id = data.getLong("id")
        val name = data.getString("name")
        val vip = data.getBoolean("vip")

        val tags = data.getJSONArray("tags")
        val tagList = (0 until tags.length()).map { tags.getString(it) }

        set(
            "🔍 解析",
            "· code=$code msg=$msg",
            "· id=$id name=$name vip=$vip",
            "· tags=$tagList",
        )
    }

    /// 遍历 —— keys() 返回 Iterator<String>
    private fun iterate() {
        val root = JSONObject(sampleJson)
        val data = root.getJSONObject("data")

        val lines = mutableListOf("🔁 遍历 data 字段：")
        val it = data.keys()
        while (it.hasNext()) {
            val k = it.next()
            val v = data.get(k)
            /// 打印每个键的运行时类型，便于理解 JSONObject 的取值机制
            lines += "· $k = $v  (${v::class.java.simpleName})"
        }
        set(*lines.toTypedArray())
    }

    /// 安全访问 —— opt 系列不抛异常，缺省返回给定默认值
    private fun safeAccess() {
        val root = JSONObject(sampleJson)
        val data = root.getJSONObject("data")

        /// key 不存在，opt 返回默认值
        val nick = data.optString("nick", "(未设置)")
        val age = data.optInt("age", -1)
        val vip = data.optBoolean("vip", false)

        /// 对可能为 null 的字段判断
        val note = if (data.has("note") && !data.isNull("note")) data.getString("note") else "无备注"

        set(
            "🛡️ 安全访问 opt*",
            "· optString(\"nick\", 默认) = $nick",
            "· optInt(\"age\", -1) = $age",
            "· optBoolean(\"vip\", false) = $vip",
            "· has+isNull 处理 note = $note",
        )
    }

    private fun set(vararg lines: String) {
        log.text = lines.joinToString("\n")
    }
}
