package com.guyi.demo1.data.network

/**
 * API 配置
 */
object ApiConfig {
    // Android 模拟器访问本机使用 10.0.2.2
    const val BASE_URL_DEV = "http://10.0.2.2:9000"
    const val BASE_URL_PROD = "https://your-domain.com"

    // 当前使用的 URL
    const val BASE_URL = BASE_URL_DEV

    // 超时配置（秒）
    const val TIMEOUT_CONNECT = 15L
    const val TIMEOUT_READ = 30L
    const val TIMEOUT_WRITE = 30L
    const val TIMEOUT_SSE = 300L  // SSE 长连接超时
}
