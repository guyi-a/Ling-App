package com.guyi.demo1.data.remote.api

import android.util.Log
import com.google.gson.Gson
import com.guyi.demo1.data.remote.dto.ChatRequest
import com.guyi.demo1.data.remote.websocket.*
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.StreamEventHandler
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SseClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val gson = Gson()
    private val TAG = "SseClient"

    fun streamChat(
        baseUrl: String,
        token: String,
        request: ChatRequest
    ): Flow<SseEvent> = callbackFlow {
        val requestBody = gson.toJson(request)
            .toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url("$baseUrl/api/chat/stream")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Accept", "text/event-stream")
            .build()

        val eventHandler = object : StreamEventHandler {
            override fun onOpen() {
                Log.d(TAG, "SSE connection opened")
            }

            override fun onMessage(eventType: String?, messageEvent: MessageEvent) {
                val data = messageEvent.data
                Log.d(TAG, "Received event: type=$eventType, data=$data")

                val event = parseEvent(eventType, data)
                event?.let {
                    trySend(it).isSuccess
                }
            }

            override fun onComment(comment: String?) {
                Log.d(TAG, "Comment: $comment")
            }

            override fun onError(t: Throwable?) {
                Log.e(TAG, "SSE error", t)
                trySend(SseEvent.Error(t?.message ?: "Unknown error"))
                close(t)
            }

            override fun onClosed() {
                Log.d(TAG, "SSE connection closed")
                channel.close()
            }
        }

        val eventSource = EventSource.Builder(eventHandler, URI.create("$baseUrl/api/chat/stream"))
            .client(okHttpClient)
            .build()

        // 注意：EventSource 不直接支持 POST，我们需要使用 OkHttp 的方式
        // 这里我们使用 LaunchDarkly 的库，它需要特殊处理
        // 作为替代方案，我们直接使用 OkHttp SSE

        val call = okHttpClient.newCall(httpRequest)
        val response = call.execute()

        if (!response.isSuccessful) {
            close(Exception("HTTP ${response.code}: ${response.message}"))
            return@callbackFlow
        }

        val source = response.body?.source()
        if (source == null) {
            close(Exception("Empty response body"))
            return@callbackFlow
        }

        try {
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break

                if (line.startsWith("event:")) {
                    val eventType = line.substring(6).trim()
                    val dataLine = source.readUtf8Line() ?: break

                    if (dataLine.startsWith("data:")) {
                        val data = dataLine.substring(5).trim()
                        val event = parseEvent(eventType, data)
                        event?.let { trySend(it) }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading SSE stream", e)
            trySend(SseEvent.Error(e.message ?: "Stream read error"))
        } finally {
            response.close()
        }

        awaitClose {
            response.close()
        }
    }

    private fun parseEvent(type: String?, data: String): SseEvent? {
        return try {
            when (type) {
                "session" -> {
                    val sessionData = gson.fromJson(data, SessionData::class.java)
                    SseEvent.Session(sessionData)
                }
                "token" -> {
                    val tokenData = gson.fromJson(data, TokenData::class.java)
                    SseEvent.Token(tokenData.text)
                }
                "model_start" -> SseEvent.ModelStart
                "tool_start" -> {
                    val toolData = gson.fromJson(data, ToolData::class.java)
                    SseEvent.ToolStart(toolData.toolName)
                }
                "tool_end" -> {
                    val toolData = gson.fromJson(data, ToolData::class.java)
                    SseEvent.ToolEnd(toolData.toolName)
                }
                "approval_required" -> {
                    val approvalData = gson.fromJson(data, ApprovalData::class.java)
                    SseEvent.ApprovalRequired(
                        requestId = approvalData.requestId,
                        toolName = approvalData.toolName,
                        toolInput = approvalData.toolInput
                    )
                }
                "approval_rejected" -> {
                    val toolData = gson.fromJson(data, ToolData::class.java)
                    SseEvent.ApprovalRejected(toolData.toolName)
                }
                "done" -> SseEvent.Done
                "error" -> {
                    val errorData = gson.fromJson(data, ErrorData::class.java)
                    SseEvent.Error(errorData.message)
                }
                else -> {
                    Log.w(TAG, "Unknown event type: $type")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing event: type=$type, data=$data", e)
            null
        }
    }
}
