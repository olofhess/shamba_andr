package com.example.shambasql.model

data class FieldBoundaryPoint(
    val id: Long = 0,
    val fieldId: Long,         // Foreign key to FarmField
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long        // Unix timestamp in milliseconds
)
