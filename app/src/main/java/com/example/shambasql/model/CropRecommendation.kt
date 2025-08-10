package com.example.shambasql.model

data class CropRecommendation(
    val id: Long = 0,
    val country: String,                   // e.g., "Kenya"
    val region: String,                    // e.g., "Kiambu"
    val cropType: String,                 // e.g., "Maize"
    val seedType: String,                 // e.g., "Jitale 634"
    val seedRateKgPerHa: Double,          // kg per hectare
    val rowSpace: Double,                 // cm between rows
    val plantSpace: Double,               // cm between plants
    val basalFertilizerKgPerHa: Double,   // kg/ha
    val topdressFertilizerKgPerHa: Double,// kg/ha
    val notes: String = ""                // Optional user notes
)

