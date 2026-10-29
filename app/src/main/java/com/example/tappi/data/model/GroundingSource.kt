package com.example.tappi.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GroundingSource(
    val title: String,
    val uri: String
)
