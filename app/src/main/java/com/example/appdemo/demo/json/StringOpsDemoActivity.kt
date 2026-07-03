package com.example.appdemo.demo.json

import android.os.Bundle
import android.util.Base64
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.Locale

/**
 * 字符串常用操作演示 ——
 *   1) 拼接：+ / StringBuilder / joinToString
 *   2) 格式化：String.format / Kotlin 模板 / getString(resId, args)
 *   3) 分割与截取：split / substring / take / drop
 *   4) 替换：replace / replaceFirst / 正则替换
 *   5) 大小写、去空白、判空
 *   6) 正则：matches / find / groupValues
 *   7) 编码：Base64 / URL / MD5
 */
class StringOpsDemoActivity : AppCompatActivity() {

    private lateinit var log: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_json_string_ops)
        setupDemoToolbar(R.string.json_string_ops_title, R.id.json_string_ops_root)

        log = findViewById(R.id.tv_string_ops_log)
        val input = findViewById<EditText>(R.id.et_string_ops_input)

        findViewById<TextView>(R.id.btn_string_concat).setOnClickListener { concat(input.text.toString()) }
        findViewById<TextView>(R.id.btn_string_format).setOnClickListener { format(input.text.toString()) }
        findViewById<TextView>(R.id.btn_string_split).setOnClickListener { splitAndPart(input.text.toString()) }
        findViewById<TextView>(R.id.btn_string_replace).setOnClickListener { replace(input.text.toString()) }
        findViewById<TextView>(R.id.btn_string_case).setOnClickListener { caseTrim(input.text.toString()) }
        findViewById<TextView>(R.id.btn_string_regex).setOnClickListener { regex(input.text.toString()) }
        findViewById<TextView>(R.id.btn_string_encode).setOnClickListener { encode(input.text.toString()) }
        findViewById<TextView>(R.id.btn_string_clear).setOnClickListener { log.text = getString(R.string.json_log_hint) }
    }

    /// 1. 字符串拼接的三种写法
    private fun concat(raw: String) {
        val fruits = listOf("苹果", "香蕉", "橘子")

        /// 直接用 + 拼接（可读但会产生临时对象，循环里慎用）
        val a = "输入=" + raw + "，长度=" + raw.length

        /// StringBuilder，循环里首选
        val b = StringBuilder().apply {
            append("列表：[")
            fruits.forEachIndexed { i, s -> if (i > 0) append(", "); append(s) }
            append("]")
        }.toString()

        /// Kotlin joinToString，最简洁
        val c = fruits.joinToString(separator = " · ", prefix = "「", postfix = "」")

        clearAppend(
            "1️⃣ 字符串拼接",
            "· `+` 直接拼：$a",
            "· StringBuilder：$b",
            "· joinToString：$c",
        )
    }

    /// 2. 字符串格式化
    private fun format(raw: String) {
        val name = raw.ifBlank { "游客" }

        /// String.format 支持 %s / %d / %.2f 等占位符
        val a = String.format(Locale.getDefault(), "你好 %s，你的积分是 %d，命中率 %.1f%%", name, 1250, 87.4523)

        /// Kotlin 字符串模板，编译期展开，无参数错位问题
        val b = "你好 $name，长度=${raw.length}"

        /// 从 strings.xml 里拿带占位符的模板
        val c = getString(R.string.json_string_format_res, name, raw.length)

        clearAppend(
            "2️⃣ 字符串格式化",
            "· String.format：$a",
            "· 模板字符串：$b",
            "· getString 模板：$c",
        )
    }

    /// 3. 分割与截取
    private fun splitAndPart(raw: String) {
        val csv = raw.ifBlank { "red,green,blue,yellow" }

        /// split 支持 String 或正则
        val parts = csv.split(",").map { it.trim() }

        /// 截取：substring / take / drop / substringBefore / substringAfter
        val head = csv.take(3)
        val tail = csv.takeLast(3)
        val body = csv.substringAfter(",", missingDelimiterValue = csv)

        clearAppend(
            "3️⃣ 分割与截取",
            "· split：$parts",
            "· take(3)：$head",
            "· takeLast(3)：$tail",
            "· substringAfter(,)：$body",
        )
    }

    /// 4. 替换
    private fun replace(raw: String) {
        val src = raw.ifBlank { "手机号 138-1234-5678 请勿外传" }

        /// 普通替换
        val a = src.replace(" ", "_")

        /// 正则替换：给手机号打码
        val b = src.replace(Regex("""(\d{3})-(\d{4})-(\d{4})"""), "$1-****-$3")

        /// 只替换第一次
        val c = src.replaceFirst("手机号", "☎")

        clearAppend(
            "4️⃣ 替换",
            "· replace 空格：$a",
            "· 正则脱敏：$b",
            "· replaceFirst：$c",
        )
    }

    /// 5. 大小写 / 去空白 / 判空
    private fun caseTrim(raw: String) {
        val src = raw.ifBlank { "  Hello Android  " }

        val a = src.trim()
        val b = src.uppercase(Locale.ROOT)
        val c = src.lowercase(Locale.ROOT)
        val d = src.replaceFirstChar { it.uppercase() }
        val empty1 = "".isNullOrBlank()
        val empty2 = "   ".isNullOrBlank()

        clearAppend(
            "5️⃣ 大小写 / 去空白 / 判空",
            "· trim：[$a]",
            "· uppercase：[$b]",
            "· lowercase：[$c]",
            "· 首字母大写：[$d]",
            "· \"\".isNullOrBlank()=$empty1",
            "· \"   \".isNullOrBlank()=$empty2",
        )
    }

    /// 6. 正则
    private fun regex(raw: String) {
        val src = raw.ifBlank { "订单#A1024 金额¥98.50 时间 2026-07-03" }

        /// 是否为合法邮箱
        val emailRegex = Regex("""^[\w.+-]+@[\w-]+(\.[\w-]+)+$""")
        val isEmail = emailRegex.matches("hi@example.com")

        /// 提取全部数字块
        val nums = Regex("""\d+""").findAll(src).map { it.value }.toList()

        /// 分组捕获
        val order = Regex("""#(\w+)\s+金额¥(\d+\.\d+)""").find(src)
        val orderInfo = order?.let { "单号=${it.groupValues[1]} 金额=${it.groupValues[2]}" } ?: "未匹配"

        clearAppend(
            "6️⃣ 正则",
            "· matches 邮箱=$isEmail",
            "· 数字块：$nums",
            "· 分组：$orderInfo",
        )
    }

    /// 7. 编码 / 哈希
    private fun encode(raw: String) {
        val src = raw.ifBlank { "你好, Android!" }
        val bytes = src.toByteArray(Charsets.UTF_8)

        /// Base64 编码 / 解码
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val fromB64 = String(Base64.decode(b64, Base64.NO_WRAP), Charsets.UTF_8)

        /// URL 编码：中文和特殊字符会转成 %xx
        val urlEnc = URLEncoder.encode(src, "UTF-8")
        val urlDec = URLDecoder.decode(urlEnc, "UTF-8")

        /// MD5 摘要（不可逆）
        val md5 = MessageDigest.getInstance("MD5").digest(bytes)
            .joinToString("") { "%02x".format(it) }

        clearAppend(
            "7️⃣ 编码 / 哈希",
            "· Base64：$b64",
            "· Base64 解码：$fromB64",
            "· URL 编码：$urlEnc",
            "· URL 解码：$urlDec",
            "· MD5：$md5",
        )
    }

    private fun clearAppend(vararg lines: String) {
        log.text = lines.joinToString("\n")
    }
}
