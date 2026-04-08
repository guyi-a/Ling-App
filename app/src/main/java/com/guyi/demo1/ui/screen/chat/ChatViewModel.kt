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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
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

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    init {
        // 如果有会话 ID，加载历史消息
        initialSessionId?.let {
            loadHistory(it)
        }
    }

    /**
     * 加载会话历史
     * 参考 web 端逻辑：解析 extra_data 中的 tool_calls，跳过 tool 角色消息，合并连续同角色消息
     */
    fun loadHistory(sessionId: String) {
        viewModelScope.launch {
            val result = messageRepository.getConversationHistory(sessionId)
            if (result.isSuccess) {
                val history = result.getOrNull()
                if (history != null) {
                    // Step 1: 转换每条消息为带 parts 的 Message，跳过 tool 角色
                    val rawMessages = history.messages
                        .filter { it.role == "user" || it.role == "assistant" }
                        .mapIndexed { idx, historyMsg ->
                            val parts = mutableListOf<MessagePart>()

                            // 解析 extra_data
                            val toolCalls = parseToolCalls(historyMsg.extraData)

                            if (historyMsg.role == "assistant" && toolCalls.isNotEmpty()) {
                                // assistant 消息带 tool_calls：先文本，再工具
                                if (historyMsg.content.isNotBlank()) {
                                    parts.add(MessagePart(type = "text", content = historyMsg.content))
                                }
                                toolCalls.forEach { toolName ->
                                    parts.add(MessagePart(
                                        type = "tool",
                                        toolName = toolName,
                                        toolStatus = "done"
                                    ))
                                }
                            } else {
                                // 普通消息
                                if (historyMsg.content.isNotBlank()) {
                                    parts.add(MessagePart(type = "text", content = historyMsg.content))
                                }
                            }

                            Message(
                                id = "history-$sessionId-$idx",
                                messageId = historyMsg.messageId,
                                role = historyMsg.role,
                                parts = parts
                            )
                        }
                        .filter { it.parts.isNotEmpty() }

                    // Step 2: 合并连续同角色消息
                    val mergedMessages = mutableListOf<Message>()
                    for (msg in rawMessages) {
                        val lastMsg = mergedMessages.lastOrNull()
                        if (lastMsg != null && lastMsg.role == msg.role) {
                            // 合并 parts 到上一条消息
                            mergedMessages[mergedMessages.lastIndex] = lastMsg.copy(
                                parts = lastMsg.parts + msg.parts
                            )
                        } else {
                            mergedMessages.add(msg)
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        messages = mergedMessages,
                        currentSessionId = sessionId
                    )
                }
            }
        }
    }

    /**
     * 从 extra_data JSON 字符串解析 tool_calls 名称列表
     */
    private fun parseToolCalls(extraData: String?): List<String> {
        if (extraData.isNullOrBlank()) return emptyList()
        return try {
            val jsonElement = json.parseToJsonElement(extraData)
            val toolCalls = jsonElement.jsonObject["tool_calls"]?.jsonArray ?: return emptyList()
            toolCalls.mapNotNull { tc ->
                val obj = tc.jsonObject
                val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                // 如果是 Skill 工具，从 args 中提取实际技能名
                if (name == "Skill") {
                    obj["args"]?.jsonObject?.get("skill")?.jsonPrimitive?.content ?: name
                } else {
                    name
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 发送消息
     */
    @Suppress("UNCHECKED_CAST")
    fun sendMessage(content: String, attachments: List<Map<String, String>>? = null) {
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
                        if (!attachments.isNullOrEmpty()) {
                            put("attachments", kotlinx.serialization.json.buildJsonArray {
                                attachments.forEach { att ->
                                    add(buildJsonObject {
                                        att.forEach { (k, v) -> put(k, v) }
                                    })
                                }
                            })
                        }
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
        _uiState.value = _uiState.value.copy(
            isStreaming = false,
            messages = _uiState.value.messages.map { msg ->
                if (msg.isStreaming) msg.copy(isStreaming = false) else msg
            }
        )
    }

    /**
     * 审批工具调用
     */
    fun approveToolUse(requestId: String, approved: Boolean) {
        viewModelScope.launch {
            try {
                val response = chatApi.approveToolUse(
                    com.guyi.demo1.data.api.ApprovalRequest(
                        request_id = requestId,
                        approved = approved
                    )
                )

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

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * 删除消息
     */
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            val result = messageRepository.deleteMessage(messageId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages.filter { it.messageId != messageId }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "删除失败: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    /**
     * 重新生成最后一条 AI 消息
     */
    fun regenerateLastMessage() {
        val messages = _uiState.value.messages
        val lastAssistant = messages.lastOrNull { it.role == "assistant" } ?: return
        val lastUser = messages.lastOrNull { it.role == "user" } ?: return

        val userContent = lastUser.parts
            .filter { it.type == "text" }
            .mapNotNull { it.content }
            .joinToString("\n")

        viewModelScope.launch {
            // 删除最后一条 AI 消息
            lastAssistant.messageId?.let { messageRepository.deleteMessage(it) }
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages.filter { it.id != lastAssistant.id }
            )
            // 重发
            sendMessage(userContent)
        }
    }

    /**
     * 编辑用户消息并重发
     */
    fun editAndResend(message: Message, newContent: String) {
        val sessionId = _uiState.value.currentSessionId ?: return
        val messageId = message.messageId ?: return

        viewModelScope.launch {
            // 删除该消息及之后的所有消息
            val result = messageRepository.deleteMessagesAfter(sessionId, messageId)
            if (result.isSuccess) {
                // 从 UI 中移除该消息及之后的消息
                val idx = _uiState.value.messages.indexOfFirst { it.id == message.id }
                if (idx >= 0) {
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages.subList(0, idx)
                    )
                }
                // 重发编辑后的内容
                sendMessage(newContent)
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "编辑失败: ${result.exceptionOrNull()?.message}"
                )
            }
        }
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
