package com.example.tappi.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageRole {
    USER,
    ASSISTANT
}

@Serializable
data class Message(
    val id: String,
    val role: MessageRole,
    val content: String,
    val sources: List<GroundingSource> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)
