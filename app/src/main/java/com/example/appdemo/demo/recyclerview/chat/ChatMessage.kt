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

data class ChatMessage(
    val text: String,
    val fromMe: Boolean,
    val time: String,
)

object FakeChats {
    fun forTitle(title: String): List<ChatMessage> = when (title) {
        "文件传输助手" -> listOf(
            recv("欢迎使用文件传输助手", "昨天 09:00"),
            recv("你可以把文件、链接、文字发到这里，方便在不同设备间同步。", "昨天 09:00"),
            sent("收到，先存个备忘", "昨天 09:05"),
            sent("明天 9 点开会，地点：会议室 B302", "昨天 09:05"),
            recv("已记录。", "昨天 09:05"),
            sent("再发个文档存这里", "昨天 14:20"),
            recv("[文件] 需求文档v2.docx", "昨天 14:20"),
            sent("还有一段链接", "昨天 14:21"),
            recv("[链接] 设计稿 v3", "昨天 14:21"),
            sent("早", "10:00"),
            recv("早", "10:00"),
            sent("帮我记一下今天的待办", "10:01"),
            sent("1. 处理代码评审", "10:01"),
            sent("2. 联调登录接口", "10:01"),
            sent("3. 整理周报", "10:01"),
            recv("已记录三项待办。", "10:01"),
            sent("再加一条：晚上 8 点提醒我喝水", "10:05"),
            recv("好的，已设置提醒。", "10:05"),
            sent("先这些，谢啦", "10:06"),
            recv("不客气。", "10:06"),
        )
        "微信团队" -> listOf(
            recv("欢迎使用微信", "昨天"),
            recv("点击右上角加号可以发起聊天或扫一扫。", "昨天"),
            recv("在「我 - 设置」中可以管理你的账号与隐私。", "昨天"),
            sent("收到，谢谢", "昨天"),
            recv("【安全提示】检测到你在新设备登录。", "昨天 22:10"),
            sent("是我本人", "昨天 22:11"),
            recv("好的，已确认安全。", "昨天 22:11"),
            recv("【新功能】消息已支持引用回复。", "10:00"),
            sent("看到了，挺好用", "10:02"),
            recv("如有问题可随时反馈。", "10:02"),
        )
        "妈妈" -> listOf(
            recv("吃饭了吗", "昨天 12:00"),
            sent("刚吃完", "昨天 12:01"),
            recv("吃的什么", "昨天 12:01"),
            sent("食堂的牛肉面", "昨天 12:02"),
            recv("好吃就行，别老点外卖", "昨天 12:02"),
            sent("嗯", "昨天 12:03"),
            recv("早点睡，别熬夜了", "昨天 23:30"),
            sent("好", "昨天 23:30"),
            recv("周末回家吗", "10:00"),
            sent("看情况，可能周日回去", "10:01"),
            recv("回来妈给你做红烧肉", "10:01"),
            sent("哈哈好", "10:02"),
            recv("路上注意安全", "10:02"),
            sent("知道啦", "10:02"),
            recv("钱够花吗", "10:05"),
            sent("够的，不用担心", "10:06"),
            recv("不够说一声", "10:06"),
            sent("嗯嗯", "10:06"),
        )
        "产品交流群" -> listOf(
            recv("新版本已经发了，麻烦看下", "09:30", who = "产品-小李"),
            recv("看了，整体没问题，文案再调一下", "09:35", who = "设计-阿俊"),
            recv("首页的引导图我重新切了一版", "09:36", who = "设计-阿俊"),
            sent("收到，下午我跟进", "09:40"),
            recv("辛苦", "09:41", who = "产品-小李"),
            recv("接口那边有个字段确认一下", "10:00", who = "后端-阿伟"),
            recv("user.profile.avatar 这个字段是必填吗？", "10:00", who = "后端-阿伟"),
            sent("非必填，可以为空字符串", "10:02"),
            recv("好的", "10:02", who = "后端-阿伟"),
            recv("@所有人 周会改到下午 4 点", "10:30", who = "产品-小李"),
            sent("收到", "10:31"),
            recv("收到", "10:32", who = "设计-阿俊"),
            recv("收到", "10:32", who = "后端-阿伟"),
            recv("会议室换 B302", "10:33", who = "产品-小李"),
            sent("ok", "10:33"),
        )
        else -> listOf(
            recv("在吗？", "昨天 20:10"),
            sent("在的", "昨天 20:11"),
            recv("有个事想问下", "昨天 20:11"),
            sent("说", "昨天 20:11"),
            recv("我们之前那个组件库还在维护吗", "昨天 20:12"),
            sent("还在，最近刚发了 1.4", "昨天 20:13"),
            recv("有更新日志吗", "昨天 20:13"),
            sent("README 里有", "昨天 20:14"),
            recv("好的我去看看", "昨天 20:14"),
            recv("看完了，挺好的", "昨天 21:00"),
            sent("有问题随时说", "昨天 21:01"),
            recv("早", "08:30"),
            sent("早", "08:30"),
            recv("今天忙吗", "09:00"),
            sent("还行，下午有个会", "09:01"),
            recv("那中午一起吃个饭？", "09:02"),
            sent("好啊，老地方", "09:02"),
            recv("12 点见", "09:03"),
            sent("ok", "09:03"),
        )
    }

    private fun sent(text: String, time: String) =
        ChatMessage(text = text, fromMe = true, time = time)

    private fun recv(text: String, time: String, who: String? = null) =
        ChatMessage(
            text = if (who != null) "$who：$text" else text,
            fromMe = false,
            time = time,
        )
}
