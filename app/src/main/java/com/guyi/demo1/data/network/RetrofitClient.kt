package com.guyi.demo1.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端单例
 */
object RetrofitClient {

    // JSON 序列化配置
    private val json = Json {
        ignoreUnknownKeys = true  // 忽略未知字段
        coerceInputValues = true  // 类型强制转换
        isLenient = true  // 宽松模式
    }

    /**
     * 创建 OkHttpClient
     */
    fun createOkHttpClient(tokenProvider: () -> String?): OkHttpClient {
        return OkHttpClient.Builder()
            // 超时配置
            .connectTimeout(ApiConfig.TIMEOUT_CONNECT, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.TIMEOUT_READ, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.TIMEOUT_WRITE, TimeUnit.SECONDS)
            // 添加拦截器
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(createLoggingInterceptor())
            .build()
    }

    /**
     * 创建日志拦截器（仅 Debug 模式）
     */
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * 创建 Retrofit 实例
     */
    fun createRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /**
     * 创建 API 服务
     */
    inline fun <reified T> createService(retrofit: Retrofit): T {
        return retrofit.create(T::class.java)
    }
}
