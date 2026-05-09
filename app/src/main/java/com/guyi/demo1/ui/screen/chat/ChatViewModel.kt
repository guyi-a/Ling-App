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
    val error: String? = null,
    val streamingStatus: String? = null // "thinking" | "compacting" | "handoff:xxx" | null
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
                    val all = history.messages

                    // Step 1: 转换 user / assistant 消息为带 parts 的 Message
                    // 用 origIdx → rawIdx 映射，方便后面按原顺序回填 tool_output
                    val rawMessages = mutableListOf<Message>()
                    val origToRaw = mutableMapOf<Int, Int>()
                    all.forEachIndexed { origIdx, historyMsg ->
                        if (historyMsg.role != "user" && historyMsg.role != "assistant") return@forEachIndexed

                        val parts = mutableListOf<MessagePart>()
                        val toolCalls = parseToolCalls(historyMsg.extraData)
                        val handoffInfo = parseHandoff(historyMsg.extraData)

                        if (historyMsg.role == "assistant" && toolCalls.isNotEmpty()) {
                            if (historyMsg.content.isNotBlank()) {
                                parts.add(MessagePart(type = "text", content = historyMsg.content))
                            }
                            toolCalls.forEach { info ->
                                parts.add(MessagePart(
                                    type = "tool",
                                    toolName = info.name,
                                    toolStatus = "done",
                                    toolInput = info.input,
                                    isSkill = info.isSkill
                                ))
                            }
                        } else if (historyMsg.role == "assistant" && handoffInfo != null) {
                            if (historyMsg.content.isNotBlank()) {
                                parts.add(MessagePart(type = "text", content = historyMsg.content))
                            }
                            parts.add(MessagePart(
                                type = "handoff",
                                agentName = handoffInfo.first,
                                handoffDirection = handoffInfo.second
                            ))
                        } else {
                            if (historyMsg.content.isNotBlank()) {
                                parts.add(MessagePart(type = "text", content = historyMsg.content))
                            }
                        }

                        if (parts.isEmpty()) return@forEachIndexed

                        origToRaw[origIdx] = rawMessages.size
                        rawMessages.add(
                            Message(
                                id = "history-$sessionId-$origIdx",
                                messageId = historyMsg.messageId,
                                role = historyMsg.role,
                                parts = parts
                            )
                        )
                    }

                    // Step 2: 回填 tool 消息内容到前一条 assistant 的第一个缺 output 的 tool part
                    all.forEachIndexed { origIdx, m ->
                        if (m.role == "tool" && m.content.isNotBlank()) {
                            for (j in origIdx - 1 downTo 0) {
                                val rawIdx = origToRaw[j] ?: continue
                                val assistantMsg = rawMessages[rawIdx]
                                if (assistantMsg.role != "assistant") continue
                                val partsMut = assistantMsg.parts.toMutableList()
                                val target = partsMut.indexOfFirst { it.type == "tool" && it.toolOutput == null }
                                if (target >= 0) {
                                    partsMut[target] = partsMut[target].copy(toolOutput = m.content)
                                    rawMessages[rawIdx] = assistantMsg.copy(parts = partsMut)
                                }
                                break
                            }
                        }
                    }

                    // Step 3: 合并连续同角色消息
                    val mergedMessages = mutableListOf<Message>()
                    for (msg in rawMessages) {
                        val lastMsg = mergedMessages.lastOrNull()
                        if (lastMsg != null && lastMsg.role == msg.role) {
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

    /** 从 extra_data 解析出来的工具调用信息 */
    private data class ToolCallInfo(
        val name: String,  // 保留原始 name（Skill 就是 Skill）
        val input: kotlinx.serialization.json.JsonObject?,
        val isSkill: Boolean
    )

    /**
     * 从 extra_data JSON 字符串解析 tool_calls
     * Skill 保留原始名，args 作为 toolInput，isSkill = true
     */
    private fun parseToolCalls(extraData: String?): List<ToolCallInfo> {
        if (extraData.isNullOrBlank()) return emptyList()
        return try {
            val jsonElement = json.parseToJsonElement(extraData)
            val toolCalls = jsonElement.jsonObject["tool_calls"]?.jsonArray ?: return emptyList()
            toolCalls.mapNotNull { tc ->
                val obj = tc.jsonObject
                val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val args = obj["args"]?.let { it as? kotlinx.serialization.json.JsonObject }
                ToolCallInfo(
                    name = name,
                    input = args,
                    isSkill = name == "Skill"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 从 extra_data JSON 字符串解析 handoff 信息
     * @return Pair(agentName, direction) 或 null
     */
    private fun parseHandoff(extraData: String?): Pair<String, String>? {
        if (extraData.isNullOrBlank()) return null
        return try {
            val jsonElement = json.parseToJsonElement(extraData)
            val handoff = jsonElement.jsonObject["handoff"]?.jsonObject ?: return null
            val to = handoff["to"]?.jsonPrimitive?.content ?: return null
            val direction = handoff["direction"]?.jsonPrimitive?.content ?: "to"
            Pair(to, direction)
        } catch (e: Exception) {
            null
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
                    val url = "${com.guyi.demo1.data.network.ApiConfig.BASE_URL}/api/chat/stream"

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

                    val streamState = StreamState(aiMessageId)
                    var completed = false

                    eventFlow.collect { event ->
                        val result = handleSSEEvent(event, streamState, userMessage.id)
                        if (result == EventResult.STREAM_END) completed = true
                    }

                    // 流结束但不是正常完成（可能是网络断开），尝试 resume
                    if (!completed) {
                        tryResumeStream(streamState)
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // 连接异常，尝试 resume
                    val sessionId = _uiState.value.currentSessionId
                    if (sessionId != null && _uiState.value.isStreaming) {
                        tryResumeStream(StreamState(aiMessageId))
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isStreaming = false,
                            error = e.message
                        )
                    }
                }
            }
        }
    }

    private enum class EventResult { CONTINUE, STREAM_END }

    private class StreamState(val aiMessageId: String) {
        var accumulated = ""
        var lastPartWasTool = false
        var userLocalId: String? = null
    }

    private fun handleSSEEvent(
        event: SSEEvent,
        state: StreamState,
        userLocalId: String? = null
    ): EventResult {
        val aiMessageId = state.aiMessageId
        when (event) {
            is SSEEvent.SessionEvent -> {
                _uiState.value = _uiState.value.copy(currentSessionId = event.sessionId)
                if (userLocalId != null) {
                    event.userMessageId?.let { updateMessageId(userLocalId, it) }
                }
            }
            is SSEEvent.ModelStartEvent -> {
                _uiState.value = _uiState.value.copy(streamingStatus = "thinking")
            }
            is SSEEvent.TokenEvent -> {
                _uiState.value = _uiState.value.copy(streamingStatus = null)
                if (state.lastPartWasTool) {
                    state.accumulated = ""
                    state.lastPartWasTool = false
                }
                state.accumulated += event.text
                val text = state.accumulated
                updateAIMessage(aiMessageId) { msg ->
                    val parts = msg.parts.toMutableList()
                    if (parts.isNotEmpty() && parts.last().type == "text") {
                        parts[parts.lastIndex] = MessagePart(type = "text", content = text)
                    } else {
                        parts.add(MessagePart(type = "text", content = text))
                    }
                    msg.copy(parts = parts)
                }
            }
            is SSEEvent.ToolGeneratingEvent -> {
                // 事件顺序：tool_generating → tool_start → tool_end
                // 先 push 一个 generating 占位（只有 toolName，没有 input）
                _uiState.value = _uiState.value.copy(streamingStatus = null)
                state.lastPartWasTool = true
                updateAIMessage(aiMessageId) { msg ->
                    msg.copy(parts = msg.parts + MessagePart(
                        type = "tool",
                        toolName = event.toolName,
                        toolStatus = "generating"
                    ))
                }
            }
            is SSEEvent.ToolStartEvent -> {
                // 找最后一个 generating 占位升级为 pending；没有就新建
                state.lastPartWasTool = true
                val isSkill = event.toolName == "Skill"
                updateAIMessage(aiMessageId) { msg ->
                    val parts = msg.parts.toMutableList()
                    var upgraded = false
                    for (i in parts.lastIndex downTo 0) {
                        if (parts[i].type == "tool" && parts[i].toolStatus == "generating") {
                            parts[i] = parts[i].copy(
                                toolName = event.toolName,
                                toolStatus = "pending",
                                toolInput = event.toolInput,
                                isSkill = isSkill
                            )
                            upgraded = true
                            break
                        }
                    }
                    if (!upgraded) {
                        parts.add(MessagePart(
                            type = "tool",
                            toolName = event.toolName,
                            toolStatus = "pending",
                            toolInput = event.toolInput,
                            isSkill = isSkill
                        ))
                    }
                    msg.copy(parts = parts)
                }
            }
            is SSEEvent.ToolEndEvent -> {
                // 找最后一个 pending/generating 的 tool part 标 done + 保存 output
                // 不再用 toolName 匹配（Skill 的 displayName 可能变化会导致匹配失败）
                updateAIMessage(aiMessageId) { msg ->
                    val parts = msg.parts.toMutableList()
                    for (i in parts.lastIndex downTo 0) {
                        if (parts[i].type == "tool"
                            && (parts[i].toolStatus == "pending" || parts[i].toolStatus == "generating")
                        ) {
                            parts[i] = parts[i].copy(
                                toolStatus = "done",
                                toolOutput = event.toolOutput
                            )
                            break
                        }
                    }
                    msg.copy(parts = parts)
                }
            }
            is SSEEvent.HandoffEvent -> {
                _uiState.value = _uiState.value.copy(streamingStatus = "handoff:${event.to}")
                updateAIMessage(aiMessageId) { msg ->
                    msg.copy(parts = msg.parts + MessagePart(
                        type = "handoff", agentName = event.to, handoffDirection = event.direction
                    ))
                }
            }
            is SSEEvent.CompactingEvent -> {
                _uiState.value = _uiState.value.copy(streamingStatus = "compacting")
            }
            is SSEEvent.CompactingDoneEvent -> {
                _uiState.value = _uiState.value.copy(streamingStatus = null)
            }
            is SSEEvent.ApprovalRequiredEvent -> {
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
                _uiState.value = _uiState.value.copy(isStreaming = false, streamingStatus = null)
            }
            is SSEEvent.DoneEvent -> {
                event.assistantMessageId?.let { updateMessageId(aiMessageId, it) }
                updateAIMessage(aiMessageId) { msg ->
                    msg.copy(isStreaming = false, approvalRequest = null)
                }
                _uiState.value = _uiState.value.copy(isStreaming = false, streamingStatus = null)
                return EventResult.STREAM_END
            }
            is SSEEvent.ErrorEvent -> {
                updateAIMessage(aiMessageId) { msg ->
                    msg.copy(
                        isStreaming = false,
                        parts = msg.parts + MessagePart(
                            type = "text", content = "\n\n_错误：${event.message}_"
                        )
                    )
                }
                _uiState.value = _uiState.value.copy(isStreaming = false, streamingStatus = null)
                return EventResult.STREAM_END
            }
            else -> {}
        }
        return EventResult.CONTINUE
    }

    private suspend fun tryResumeStream(state: StreamState, maxRetries: Int = 3) {
        val sessionId = _uiState.value.currentSessionId ?: return
        var retryCount = 0

        while (retryCount < maxRetries && _uiState.value.isStreaming) {
            retryCount++
            val backoff = (1000L * retryCount).coerceAtMost(5000L)
            kotlinx.coroutines.delay(backoff)

            try {
                var completed = false
                sseManager.connectResume(sessionId).collect { event ->
                    val result = handleSSEEvent(event, state)
                    if (result == EventResult.STREAM_END) completed = true
                }
                if (completed) return
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                // 重试
            }
        }

        // 重试耗尽，标记结束
        if (_uiState.value.isStreaming) {
            updateAIMessage(state.aiMessageId) { msg ->
                msg.copy(isStreaming = false)
            }
            _uiState.value = _uiState.value.copy(
                isStreaming = false,
                streamingStatus = null
            )
        }
    }

    fun stopStreaming() {
        val sessionId = _uiState.value.currentSessionId
        streamingJob?.cancel()
        streamingJob = null
        _uiState.value = _uiState.value.copy(
            isStreaming = false,
            messages = _uiState.value.messages.map { msg ->
                if (msg.isStreaming) msg.copy(isStreaming = false) else msg
            }
        )
        // 通知后端停止 Agent
        if (sessionId != null) {
            viewModelScope.launch {
                try {
                    chatApi.stopGeneration(sessionId)
                } catch (_: Exception) {}
            }
        }
    }

    fun approveToolUse(requestId: String, approved: Boolean, alwaysAllow: Boolean = false) {
        val toolName = _uiState.value.messages
            .mapNotNull { it.approvalRequest }
            .firstOrNull { it.requestId == requestId }
            ?.toolName

        viewModelScope.launch {
            try {
                chatApi.approveToolUse(
                    com.guyi.demo1.data.api.ApprovalRequest(
                        request_id = requestId,
                        approved = approved,
                        always_allow = alwaysAllow && approved,
                        tool_name = if (alwaysAllow && approved) toolName else null
                    )
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages.map { msg ->
                        if (msg.approvalRequest?.requestId == requestId) {
                            msg.copy(approvalRequest = null, isStreaming = approved)
                        } else {
                            msg
                        }
                    },
                    isStreaming = approved
                )
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
