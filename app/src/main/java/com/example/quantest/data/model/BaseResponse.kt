package com.example.quantest.data.model

data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)
