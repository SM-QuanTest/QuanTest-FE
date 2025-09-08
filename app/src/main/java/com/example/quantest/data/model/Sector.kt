package com.example.quantest.data.model

data class Sector(
    val sectorId: Int,
    val sectorName: String
)

data class SectorResponse(
    val success: Boolean,
    val message: String,
    val data: List<Sector>
)