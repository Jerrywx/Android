package com.example.appdemo.demo.network

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

/**
 * 用户数据模型 —— 对应 jsonplaceholder /users 接口返回的 JSON
 *
 * Gson 会根据字段名自动映射 JSON 中的同名键。
 */
data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String?,
    val website: String?,
)

/**
 * 文章数据模型 —— 对应 jsonplaceholder /posts 接口
 *
 * 用于演示 POST 请求体的序列化。
 */
data class Post(
    val id: Int? = null,
    val userId: Int,
    val title: String,
    val body: String,
)
