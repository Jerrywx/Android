package com.example.appdemo.demo.json

import android.os.Bundle
import android.util.JsonReader
import android.util.JsonToken
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import java.io.StringReader

/**
 * android.util.JsonReader 流式解析演示 ——
 *
 *  · 场景：数据量大（几十 MB），不想一次性 new 出全部对象；或只关心少数字段
 *  · 特点：逐 token 前进，内存占用极低；但代码相对啰嗦
 *  · 用法：beginObject / endObject / beginArray / endArray / nextName / nextX
 */
class JsonStreamDemoActivity : AppCompatActivity() {

    private lateinit var log: TextView

    /// 假想一个大数组，实际业务里可能来自 InputStream / 网络流
    private val streamJson = """
        {
          "total": 3,
          "users": [
            {"id":1,"name":"Alice","age":24,"skills":["kotlin","android"]},
            {"id":2,"name":"Bob","age":30,"skills":["go","docker","k8s"]},
            {"id":3,"name":"Carol","age":19,"skills":["compose"]}
          ]
        }
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_json_stream)
        setupDemoToolbar(R.string.json_stream_title, R.id.json_stream_root)

        log = findViewById(R.id.tv_json_stream_log)
        findViewById<TextView>(R.id.tv_json_stream_sample).text = streamJson

        findViewById<TextView>(R.id.btn_json_stream_parse).setOnClickListener { parseStream() }
        findViewById<TextView>(R.id.btn_json_stream_only_name).setOnClickListener { onlyNames() }
        findViewById<TextView>(R.id.btn_json_stream_token).setOnClickListener { tokenDump() }
        findViewById<TextView>(R.id.btn_json_stream_clear).setOnClickListener { log.text = getString(R.string.json_log_hint) }
    }

    /// 完整解析成 List<User>
    private data class UserRow(val id: Long, val name: String, val age: Int, val skills: List<String>)

    private fun parseStream() {
        val reader = JsonReader(StringReader(streamJson))
        var total = 0
        val users = mutableListOf<UserRow>()

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "total" -> total = reader.nextInt()
                "users" -> {
                    reader.beginArray()
                    while (reader.hasNext()) users += readUser(reader)
                    reader.endArray()
                }
                else -> reader.skipValue()          /// 不认识的字段直接跳过
            }
        }
        reader.endObject()
        reader.close()

        set(
            "🚿 流式完整解析",
            "· total=$total",
            *users.map { "· $it" }.toTypedArray(),
        )
    }

    /// 只读 name 字段，其余全部 skipValue —— 演示"只挑我要的"
    private fun onlyNames() {
        val reader = JsonReader(StringReader(streamJson))
        val names = mutableListOf<String>()

        reader.beginObject()
        while (reader.hasNext()) {
            if (reader.nextName() == "users") {
                reader.beginArray()
                while (reader.hasNext()) {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        if (reader.nextName() == "name") names += reader.nextString()
                        else reader.skipValue()
                    }
                    reader.endObject()
                }
                reader.endArray()
            } else reader.skipValue()
        }
        reader.endObject()
        reader.close()

        set(
            "🎯 只取 name 字段",
            "· names=$names",
            "· 优势：其他字段不构造对象，内存占用最小",
        )
    }

    /// 观察一下 token 流：JsonReader 会一个个吐出 BEGIN_OBJECT/NAME/STRING…
    private fun tokenDump() {
        val reader = JsonReader(StringReader("""{"a":1,"b":[true,null,"x"]}"""))
        val lines = mutableListOf("🔬 Token 流：")
        while (true) {
            val t = reader.peek()
            if (t == JsonToken.END_DOCUMENT) break
            when (t) {
                JsonToken.BEGIN_OBJECT -> { lines += "BEGIN_OBJECT {"; reader.beginObject() }
                JsonToken.END_OBJECT -> { lines += "END_OBJECT }"; reader.endObject() }
                JsonToken.BEGIN_ARRAY -> { lines += "BEGIN_ARRAY ["; reader.beginArray() }
                JsonToken.END_ARRAY -> { lines += "END_ARRAY ]"; reader.endArray() }
                JsonToken.NAME -> lines += "NAME → ${reader.nextName()}"
                JsonToken.STRING -> lines += "STRING → \"${reader.nextString()}\""
                JsonToken.NUMBER -> lines += "NUMBER → ${reader.nextLong()}"
                JsonToken.BOOLEAN -> lines += "BOOLEAN → ${reader.nextBoolean()}"
                JsonToken.NULL -> { lines += "NULL"; reader.nextNull() }
                else -> reader.skipValue()
            }
        }
        reader.close()
        set(*lines.toTypedArray())
    }

    private fun readUser(reader: JsonReader): UserRow {
        var id = 0L; var name = ""; var age = 0
        val skills = mutableListOf<String>()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextLong()
                "name" -> name = reader.nextString()
                "age" -> age = reader.nextInt()
                "skills" -> {
                    reader.beginArray()
                    while (reader.hasNext()) skills += reader.nextString()
                    reader.endArray()
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return UserRow(id, name, age, skills)
    }

    private fun set(vararg lines: String) {
        log.text = lines.joinToString("\n")
    }
}
