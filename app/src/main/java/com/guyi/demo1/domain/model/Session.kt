package com.guyi.demo1.domain.model

data class Session(
    val sessionId: String,
    val title: String,
    val createdAt: String,
    val updatedAt: String,
    val messageCount: Int = 0
)
