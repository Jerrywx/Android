package com.example.appdemo.network

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
