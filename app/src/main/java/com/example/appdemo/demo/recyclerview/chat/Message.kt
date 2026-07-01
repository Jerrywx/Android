package com.example.appdemo.demo.recyclerview.chat

import com.example.appdemo.R
import com.example.appdemo.common.*
import com.example.appdemo.tabs.*
import com.example.appdemo.demo.layout.*
import com.example.appdemo.demo.concurrent.*
import com.example.appdemo.demo.network.*
import com.example.appdemo.demo.animation.*
import com.example.appdemo.demo.animation.widget.*
import com.example.appdemo.demo.viewpager.*
import com.example.appdemo.demo.recyclerview.*
import com.example.appdemo.demo.recyclerview.chat.*
import com.example.appdemo.demo.fragment.*

data class Message(
    val title: String,
    val preview: String,
    val time: String,
    val avatarLetter: String,
)

object FakeMessages {
    fun build(): List<Message> {
        val titles = listOf(
            "文件传输助手", "微信团队", "张三", "李四", "王五",
            "产品交流群", "项目-Android", "Kotlin 学习圈", "妈妈", "爸爸",
            "高中同学群", "公司前端组", "赵六", "钱七", "孙八",
            "周九", "吴十", "外卖通知", "快递服务", "订阅号消息",
        )
        val previews = listOf(
            "你撤回了一条消息", "[图片]", "晚上一起吃饭吗？", "会议改到下午三点",
            "好的，收到", "新版本已经发了，麻烦看下", "[文件] 需求文档v2.docx",
            "这个 API 怎么用？", "记得早点睡", "周末回家吗",
            "[语音] 12''", "@所有人 周会取消", "在吗", "稍等",
            "刚发你了", "明天见", "ok", "您的订单已送达",
            "包裹已签收", "更新：今日推荐",
        )
        val times = listOf(
            "刚刚", "1 分钟前", "10:23", "09:45", "昨天",
            "昨天", "星期一", "星期日", "12/22", "12/21",
            "12/20", "12/19", "12/18", "12/15", "12/10",
            "12/05", "11/30", "11/28", "11/20", "11/11",
        )
        return List(20) { i ->
            val title = titles[i]
            Message(
                title = title,
                preview = previews[i],
                time = times[i],
                avatarLetter = title.first().toString(),
            )
        }
    }
}
