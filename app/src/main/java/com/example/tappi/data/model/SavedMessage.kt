package com.example.tappi.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SavedMessage(
    val id: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
