package com.guyi.demo1.data.network

import com.guyi.demo1.data.model.SSEEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.util.concurrent.TimeUnit

/**
 * SSE 流式事件管理器
 * 使用 OkHttp 直接读取 SSE 流
 */
class SSEManager(private val tokenProvider: () -> String?) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    // SSE 专用 OkHttpClient（长超时）
    private val sseClient = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.TIMEOUT_CONNECT, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.TIMEOUT_SSE, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.TIMEOUT_WRITE, TimeUnit.SECONDS)
        .build()

    /**
     * 连接 SSE 流（POST 请求）
     *
     * @param url 完整的 SSE 端点 URL
     * @param jsonBody JSON 请求体
     * @return SSEEvent 的 Flow
     */
    fun connectPost(url: String, jsonBody: String): Flow<SSEEvent> = callbackFlow {
        // 构建请求
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Accept", "text/event-stream")

        // 添加 Token
        val token = tokenProvider()
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()

        // 在后台线程执行 HTTP 请求
        launch(Dispatchers.IO) {
            try {
                val response = sseClient.newCall(request).execute()
                val reader = response.body?.charStream()?.buffered()

                if (reader != null) {
                    parseSSEStreamCallback(reader, this@callbackFlow)
                } else {
                    trySend(SSEEvent.ErrorEvent("无法读取响应流"))
                    close()
                }
            } catch (e: Exception) {
                trySend(SSEEvent.ErrorEvent("连接失败: ${e.message}"))
                close()
            }
        }

        awaitClose {
            // 流关闭时的清理工作
        }
    }

    /**
     * 连接 SSE 流（GET 请求）
     *
     * @param url 完整的 SSE 端点 URL
     * @return SSEEvent 的 Flow
     */
    suspend fun connect(url: String): Flow<SSEEvent> = withContext(Dispatchers.IO) {
        val channel = Channel<SSEEvent>(Channel.UNLIMITED)

        // 构建请求
        val requestBuilder = Request.Builder()
            .url(url)
            .get()
            .header("Accept", "text/event-stream")
            .header("Content-Type", "application/json")

        // 添加 Token
        val token = tokenProvider()
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()

        try {
            val response = sseClient.newCall(request).execute()
            val reader = response.body?.charStream()?.buffered()

            if (reader != null) {
                // 在后台线程读取流
                parseSSEStream(reader, channel)
            } else {
                channel.trySend(SSEEvent.ErrorEvent("无法读取响应流"))
                channel.close()
            }
        } catch (e: Exception) {
            channel.trySend(SSEEvent.ErrorEvent("连接失败: ${e.message}"))
            channel.close()
        }

        channel.receiveAsFlow()
    }

    /**
     * 解析 SSE 流（用于 callbackFlow）
     */
    private fun parseSSEStreamCallback(reader: BufferedReader, scope: kotlinx.coroutines.channels.ProducerScope<SSEEvent>) {
        try {
            var eventType: String? = null
            var data: StringBuilder = StringBuilder()

            reader.useLines { lines ->
                for (line in lines) {
                    when {
                        line.startsWith("event:") -> {
                            eventType = line.substring(6).trim()
                        }
                        line.startsWith("data:") -> {
                            if (data.isNotEmpty()) {
                                data.append("\n")
                            }
                            data.append(line.substring(5).trim())
                        }
                        line.isEmpty() -> {
                            // 空行表示一个事件结束
                            if (data.isNotEmpty()) {
                                val event = parseEvent(eventType ?: "message", data.toString())
                                if (event != null) {
                                    scope.trySend(event)

                                    // 如果收到 done 或 error，关闭
                                    if (event is SSEEvent.DoneEvent || event is SSEEvent.ErrorEvent) {
                                        scope.close()
                                        return
                                    }
                                }
                                // 重置
                                eventType = null
                                data = StringBuilder()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            scope.trySend(SSEEvent.ErrorEvent("读取流失败: ${e.message}"))
        } finally {
            scope.close()
        }
    }

    /**
     * 解析 SSE 流（旧版，用于 GET 请求）
     */
    private fun parseSSEStream(reader: BufferedReader, channel: Channel<SSEEvent>) {
        try {
            var eventType: String? = null
            var data: StringBuilder = StringBuilder()

            reader.useLines { lines ->
                for (line in lines) {
                    when {
                        line.startsWith("event:") -> {
                            eventType = line.substring(6).trim()
                        }
                        line.startsWith("data:") -> {
                            if (data.isNotEmpty()) {
                                data.append("\n")
                            }
                            data.append(line.substring(5).trim())
                        }
                        line.isEmpty() -> {
                            // 空行表示一个事件结束
                            if (data.isNotEmpty()) {
                                val event = parseEvent(eventType ?: "message", data.toString())
                                if (event != null) {
                                    channel.trySend(event)

                                    // 如果收到 done 或 error，关闭
                                    if (event is SSEEvent.DoneEvent || event is SSEEvent.ErrorEvent) {
                                        channel.close()
                                        return
                                    }
                                }
                                // 重置
                                eventType = null
                                data = StringBuilder()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            channel.trySend(SSEEvent.ErrorEvent("读取流失败: ${e.message}"))
        } finally {
            channel.close()
        }
    }

    /**
     * 解析 SSE 事件
     */
    private fun parseEvent(eventType: String, data: String): SSEEvent? {
        return try {
            when (eventType) {
                "session" -> {
                    json.decodeFromString<SSEEvent.SessionEvent>(data)
                }
                "token" -> {
                    val tokenData = json.parseToJsonElement(data).jsonObject["text"]?.jsonPrimitive?.content ?: ""
                    SSEEvent.TokenEvent(tokenData)
                }
                "tool_start" -> {
                    json.decodeFromString<SSEEvent.ToolStartEvent>(data)
                }
                "tool_end" -> {
                    json.decodeFromString<SSEEvent.ToolEndEvent>(data)
                }
                "approval_required" -> {
                    println("🔔 SSE 收到 approval_required")
                    println("📄 原始数据: $data")
                    json.decodeFromString<SSEEvent.ApprovalRequiredEvent>(data)
                }
                "approval_rejected" -> {
                    json.decodeFromString<SSEEvent.ApprovalRejectedEvent>(data)
                }
                "done" -> {
                    json.decodeFromString<SSEEvent.DoneEvent>(data)
                }
                "cancelled" -> {
                    SSEEvent.CancelledEvent
                }
                "error" -> {
                    json.decodeFromString<SSEEvent.ErrorEvent>(data)
                }
                else -> null
            }
        } catch (e: Exception) {
            println("❌ SSE 解析失败: eventType=$eventType, data=$data, error=${e.message}")
            e.printStackTrace()
            SSEEvent.ErrorEvent("解析 $eventType 事件失败: ${e.message}")
        }
    }
}
