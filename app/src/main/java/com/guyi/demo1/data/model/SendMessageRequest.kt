package com.guyi.demo1.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 发送消息请求
 */
@Serializable
data class SendMessageRequest(
    val message: String,
    @SerialName("session_id")
    val sessionId: String? = null,
    val attachments: List<Attachment>? = null
)
