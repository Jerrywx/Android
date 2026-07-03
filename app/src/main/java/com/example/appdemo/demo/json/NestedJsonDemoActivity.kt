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
import org.json.JSONObject

/**
 * 复杂嵌套 JSON —— 演示真实业务里常见的多层结构：
 *   {
 *     code, msg,
 *     data: {
 *       user: { profile: {...}, contacts: [...] },
 *       orders: [ { items: [...] }, ... ]
 *     }
 *   }
 *
 * 两种解法并列：
 *   1) JSONObject 手写按层取值——依赖少但代码啰嗦
 *   2) Gson 数据类直接映射——推荐做法
 */
class NestedJsonDemoActivity : AppCompatActivity() {

    private lateinit var log: TextView

    private val nestedJson = """
        {
          "code": 0,
          "msg": "ok",
          "data": {
            "user": {
              "id": 1024,
              "profile": {
                "nick": "王小明",
                "avatar": "https://cdn.example.com/1024.png",
                "level": 5
              },
              "contacts": [
                {"type": "email", "value": "wang@example.com"},
                {"type": "phone", "value": "138****5678"}
              ]
            },
            "orders": [
              {
                "orderId": "A1001",
                "amount": 128.5,
                "items": [
                  {"name": "耳机", "qty": 1, "price": 99.0},
                  {"name": "数据线", "qty": 2, "price": 14.75}
                ]
              },
              {
                "orderId": "A1002",
                "amount": 56.0,
                "items": [
                  {"name": "保护壳", "qty": 1, "price": 56.0}
                ]
              }
            ]
          }
        }
    """.trimIndent()

    /// 用数据类描述整个 JSON 结构，Gson 会一次性映射到对象树
    private data class Envelope(val code: Int, val msg: String, val data: DataBlock)
    private data class DataBlock(val user: User, val orders: List<Order>)
    private data class User(val id: Long, val profile: Profile, val contacts: List<Contact>)
    private data class Profile(val nick: String, val avatar: String, val level: Int)
    private data class Contact(val type: String, val value: String)
    private data class Order(
        @SerializedName("orderId") val orderId: String,
        val amount: Double,
        val items: List<Item>,
    )
    private data class Item(val name: String, val qty: Int, val price: Double)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_json_nested)
        setupDemoToolbar(R.string.json_nested_title, R.id.json_nested_root)

        log = findViewById(R.id.tv_json_nested_log)
        findViewById<TextView>(R.id.tv_json_nested_sample).text = nestedJson

        findViewById<TextView>(R.id.btn_json_nested_manual).setOnClickListener { manual() }
        findViewById<TextView>(R.id.btn_json_nested_gson).setOnClickListener { byGson() }
        findViewById<TextView>(R.id.btn_json_nested_stat).setOnClickListener { totalAmount() }
        findViewById<TextView>(R.id.btn_json_nested_clear).setOnClickListener { log.text = getString(R.string.json_log_hint) }
    }

    /// 手写 JSONObject 逐层取值
    private fun manual() {
        val root = JSONObject(nestedJson)
        val data = root.getJSONObject("data")

        val profile = data.getJSONObject("user").getJSONObject("profile")
        val nick = profile.getString("nick")
        val level = profile.getInt("level")

        val orders = data.getJSONArray("orders")
        val orderIds = (0 until orders.length()).map { orders.getJSONObject(it).getString("orderId") }

        set(
            "🧱 手写 JSONObject 按层取值",
            "· 用户昵称=$nick 等级=$level",
            "· 订单号列表=$orderIds",
        )
    }

    /// Gson 一次映射到对象树
    private fun byGson() {
        val env: Envelope = Gson().fromJson(nestedJson, Envelope::class.java)
        val user = env.data.user
        val orderCount = env.data.orders.size
        val itemCount = env.data.orders.sumOf { it.items.size }

        set(
            "🧠 Gson 数据类映射",
            "· code=${env.code} msg=${env.msg}",
            "· 用户 id=${user.id} nick=${user.profile.nick}",
            "· 联系方式：${user.contacts.map { "${it.type}=${it.value}" }}",
            "· 订单数=$orderCount，商品行数=$itemCount",
            "",
            "重新序列化：",
            GsonBuilder().setPrettyPrinting().create().toJson(env.data.orders),
        )
    }

    /// 综合计算：把所有订单里的所有商品金额加总
    private fun totalAmount() {
        val env: Envelope = Gson().fromJson(nestedJson, Envelope::class.java)
        val total = env.data.orders.sumOf { order ->
            order.items.sumOf { it.qty * it.price }
        }
        val fromOrderSum = env.data.orders.sumOf { it.amount }

        set(
            "🧮 统计",
            "· 按 items 明细求和 = %.2f".format(total),
            "· 按 order.amount 求和 = %.2f".format(fromOrderSum),
            "· 差额 = %.2f".format(total - fromOrderSum),
        )
    }

    private fun set(vararg lines: String) {
        log.text = lines.joinToString("\n")
    }
}
