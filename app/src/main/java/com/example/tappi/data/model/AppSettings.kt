package com.example.tappi.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val model: String = "gemini-2.5-flash",
    val useSearch: Boolean = true,
    val useMaps: Boolean = false,
    val voiceLanguage: String = "en-US",
    val isDarkMode: Boolean = false,
    val userAvatarUri: String? = null,
    val backgroundOpacity: Float = 0.12f
)
