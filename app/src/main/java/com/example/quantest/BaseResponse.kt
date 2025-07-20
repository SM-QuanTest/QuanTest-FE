package com.example.quantest

data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)

