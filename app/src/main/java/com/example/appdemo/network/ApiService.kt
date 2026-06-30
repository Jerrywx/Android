package com.example.appdemo.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit 接口定义 —— 使用注解描述 HTTP 请求。
 *
 * Retrofit 会在运行时通过动态代理生成实现类，把每个方法转换成 OkHttp 请求。
 *
 * 注解说明：
 *   @GET / @POST       声明请求方法和相对路径
 *   @Path              路径占位符（替换 {id}）
 *   @Query             查询参数（拼接到 URL 后面 ?key=value）
 *   @Body              请求体（自动序列化为 JSON）
 *
 * suspend 关键字让方法成为挂起函数，Retrofit 会在 IO 线程执行请求，
 * 调用方无需手动 enqueue / withContext。
 */
interface ApiService {

    /// 获取用户列表 GET https://jsonplaceholder.typicode.com/users
    @GET("users")
    suspend fun getUsers(): List<User>

    /// 获取单个用户 GET https://jsonplaceholder.typicode.com/users/{id}
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): User

    /// 按文章作者过滤 GET https://jsonplaceholder.typicode.com/posts?userId=1
    @GET("posts")
    suspend fun getPostsByUser(@Query("userId") userId: Int): List<Post>

    /// 创建文章 POST https://jsonplaceholder.typicode.com/posts
    @POST("posts")
    suspend fun createPost(@Body post: Post): Post
}
