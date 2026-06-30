package com.example.appdemo.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 单例 —— 全局共享一个 OkHttpClient 与 Retrofit 实例。
 *
 * - 添加日志拦截器，方便在 Logcat 查看请求与响应
 * - 统一超时配置
 * - 使用 Gson 转换器解析 JSON
 */
object RetrofitClient {

    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    /// OkHttp 客户端：添加日志拦截器 + 超时配置
    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /// Retrofit 实例：绑定 BaseUrl、OkHttp、Gson 转换器
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /// 业务接口
    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
