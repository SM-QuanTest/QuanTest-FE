package com.example.quantest.data.model

import com.google.gson.annotations.SerializedName

data class Page<T>(
    @SerializedName("contents") val contents: List<T>,
    @SerializedName("nextCursor") val nextCursor: String?,
    @SerializedName("hasNext") val hasNext: Boolean
)