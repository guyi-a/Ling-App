package com.guyi.demo1.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guyi.demo1.data.api.ChatApi
import com.guyi.demo1.data.model.Message
import com.guyi.demo1.data.model.MessagePart
import com.guyi.demo1.data.model.ApprovalRequest
import com.guyi.demo1.data.model.SSEEvent
import com.guyi.demo1.data.network.SSEManager
import com.guyi.demo1.data.repository.MessageRepository
import com.guyi.demo1.data.repository.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID

/**
 * 聊天 UI 状态
 */
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isStreaming: Boolean = false,
    val currentSessionId: String? = null,
    val error: String? = null
)

/**
 * 聊天 ViewModel
 */
class ChatViewModel(
    private val chatApi: ChatApi,
    private val sessionRepository: SessionRepository,
    private val messageRepository: MessageRepository,
    private val sseManager: SSEManager,
    initialSessionId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatUiState(currentSessionId = initialSessionId)
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null
    private val json = Json { ignoreUnknownKeys = true }

    init {
        // 如果有会话 ID，加载历史消息
        initialSessionId?.let {
            loadHistory(it)
        }
    }

    /**
     * 加载会话历史
     */
    fun loadHistory(sessionId: String) {
        viewModelScope.launch {
            val result = messageRepository.getConversationHistory(sessionId)
            if (result.isSuccess) {
                val history = result.getOrNull()
                if (history != null) {
                    val messages = history.messages.map { historyMsg ->
                        Message(
                            id = UUID.randomUUID().toString(),
                            messageId = historyMsg.messageId,
                            role = historyMsg.role,
                            parts = listOf(
                                MessagePart(
                                    type = "text",
                                    content = historyMsg.content
                                )
                            )
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        currentSessionId = sessionId
                    )
                }
            }
        }
    }

    /**
     * 发送消息
     */
    fun sendMessage(content: String, attachments: List<Any>? = null) {
        if (content.isBlank() || _uiState.value.isStreaming) return

        viewModelScope.launch {
            // 添加用户消息
            val userMessage = Message(
                id = UUID.randomUUID().toString(),
                role = "user",
                parts = listOf(MessagePart(type = "text", content = content))
            )
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + userMessage,
                isStreaming = true
            )

            // 创建 AI 消息占位
            val aiMessageId = UUID.randomUUID().toString()
            val aiMessage = Message(
                id = aiMessageId,
                role = "assistant",
                parts = emptyList(),
                isStreaming = true
            )
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + aiMessage
            )

            // 开始 SSE 流式接收
            streamingJob = viewModelScope.launch {
                try {
                    val url = "http://10.0.2.2:9000/api/chat/stream"

                    // 构建请求体
                    val requestBody = buildJsonObject {
                        put("message", content)
                        _uiState.value.currentSessionId?.let { put("session_id", it) }
                        // TODO: 添加 attachments
                    }.toString()

                    val eventFlow = sseManager.connectPost(url, requestBody)

                    var accumulated = ""
                    var lastPartWasTool = false

                    eventFlow.collect { event ->
                        when (event) {
                            is SSEEvent.SessionEvent -> {
                                // 更新会话 ID
                                _uiState.value = _uiState.value.copy(
                                    currentSessionId = event.sessionId
                                )
                                // 保存用户消息的 message_id
                                event.userMessageId?.let { msgId ->
                                    updateMessageId(userMessage.id, msgId)
                                }
                            }
                            is SSEEvent.TokenEvent -> {
                                if (lastPartWasTool) {
                                    accumulated = ""
                                    lastPartWasTool = false
                                }
                                accumulated += event.text
                                updateAIMessage(aiMessageId) { msg ->
                                    val parts = msg.parts.toMutableList()
                                    if (parts.isNotEmpty() && parts.last().type == "text") {
                                        parts[parts.lastIndex] = MessagePart(
                                            type = "text",
                                            content = accumulated
                                        )
                                    } else {
                                        parts.add(MessagePart(type = "text", content = accumulated))
                                    }
                                    msg.copy(parts = parts)
                                }
                            }
                            is SSEEvent.ToolStartEvent -> {
                                lastPartWasTool = true
                                updateAIMessage(aiMessageId) { msg ->
                                    msg.copy(
                                        parts = msg.parts + MessagePart(
                                            type = "tool",
                                            toolName = event.toolName,
                                            toolStatus = "pending"
                                        )
                                    )
                                }
                            }
                            is SSEEvent.ToolEndEvent -> {
                                updateAIMessage(aiMessageId) { msg ->
                                    val parts = msg.parts.toMutableList()
                                    for (i in parts.lastIndex downTo 0) {
                                        if (parts[i].type == "tool" &&
                                            parts[i].toolName == event.toolName &&
                                            parts[i].toolStatus == "pending"
                                        ) {
                                            parts[i] = parts[i].copy(toolStatus = "done")
                                            break
                                        }
                                    }
                                    msg.copy(parts = parts)
                                }
                            }
                            is SSEEvent.ApprovalRequiredEvent -> {
                                println("🔔 收到审批请求: toolName=${event.toolName}, requestId=${event.requestId}")

                                // 更新消息状态
                                updateAIMessage(aiMessageId) { msg ->
                                    msg.copy(
                                        isStreaming = false,
                                        approvalRequest = ApprovalRequest(
                                            requestId = event.requestId,
                                            toolName = event.toolName,
                                            toolInput = event.toolInput
                                        )
                                    )
                                }

                                // 暂停流式状态，等待用户审批
                                _uiState.value = _uiState.value.copy(isStreaming = false)

                                // 验证消息是否更新
                                val updatedMsg = _uiState.value.messages.find { it.id == aiMessageId }
                                println("✅ 消息已更新: id=$aiMessageId, hasApproval=${updatedMsg?.approvalRequest != null}")
                            }
                            is SSEEvent.DoneEvent -> {
                                updateMessageId(aiMessageId, event.assistantMessageId)
                                updateAIMessage(aiMessageId) { msg ->
                                    msg.copy(isStreaming = false, approvalRequest = null)
                                }
                                _uiState.value = _uiState.value.copy(isStreaming = false)
                            }
                            is SSEEvent.ErrorEvent -> {
                                updateAIMessage(aiMessageId) { msg ->
                                    msg.copy(
                                        isStreaming = false,
                                        parts = msg.parts + MessagePart(
                                            type = "text",
                                            content = "\n\n_错误：${event.message}_"
                                        )
                                    )
                                }
                                _uiState.value = _uiState.value.copy(isStreaming = false)
                            }
                            else -> {}
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isStreaming = false,
                        error = e.message
                    )
                }
            }
        }
    }

    /**
     * 停止流式生成
     */
    fun stopStreaming() {
        streamingJob?.cancel()
        streamingJob = null
        _uiState.value = _uiState.value.copy(isStreaming = false)
    }

    /**
     * 测试审批功能（仅用于调试）
     */
    fun testApproval() {
        val testMessage = Message(
            id = "test-${System.currentTimeMillis()}",
            role = "assistant",
            parts = listOf(MessagePart(type = "text", content = "测试审批消息")),
            isStreaming = false,
            approvalRequest = ApprovalRequest(
                requestId = "test-request-123",
                toolName = "python_repl",
                toolInput = kotlinx.serialization.json.buildJsonObject {
                    put("code", kotlinx.serialization.json.JsonPrimitive("print('Hello, World!')"))
                }
            )
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + testMessage
        )
        println("🧪 添加测试审批消息: ${testMessage.approvalRequest}")
    }

    /**
     * 审批工具调用
     */
    fun approveToolUse(requestId: String, approved: Boolean) {
        viewModelScope.launch {
            try {
                println("🎯 发送审批: requestId=$requestId, approved=$approved")

                val response = chatApi.approveToolUse(
                    com.guyi.demo1.data.api.ApprovalRequest(
                        request_id = requestId,
                        approved = approved
                    )
                )

                println("✅ 审批响应: ${response.status}")

                // 清除当前消息的审批请求状态
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages.map { msg ->
                        if (msg.approvalRequest?.requestId == requestId) {
                            msg.copy(approvalRequest = null, isStreaming = approved)
                        } else {
                            msg
                        }
                    },
                    // 如果审批通过，恢复流式状态
                    isStreaming = approved
                )

                // 如果审批通过，流会自动继续发送 token
                // 如果拒绝，会收到 approval_rejected 事件
            } catch (e: Exception) {
                println("❌ 审批失败: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "审批失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 更新消息的 message_id
     */
    private fun updateMessageId(localId: String, messageId: String) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages.map { msg ->
                if (msg.id == localId) msg.copy(messageId = messageId) else msg
            }
        )
    }

    /**
     * 更新 AI 消息
     */
    private fun updateAIMessage(aiMessageId: String, update: (Message) -> Message) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages.map { msg ->
                if (msg.id == aiMessageId) update(msg) else msg
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopStreaming()
    }
}

/**
 * ViewModel 工厂
 */
class ChatViewModelFactory(
    private val chatApi: ChatApi,
    private val sessionRepository: SessionRepository,
    private val messageRepository: MessageRepository,
    private val sseManager: SSEManager,
    private val initialSessionId: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(
                chatApi,
                sessionRepository,
                messageRepository,
                sseManager,
                initialSessionId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
