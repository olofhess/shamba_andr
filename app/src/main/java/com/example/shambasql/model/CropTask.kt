package com.example.shambasql.model

import java.util.Date

data class CropTask(
    val id: Long = 0,
    val fieldId: Long,
    val cropId: Long,
    val name: String,
    val date: Date,
    val unitType: String,
    val costPerUnit: Double,
    val quantity: Double,
    val cost: Double,
    val notes: String = ""
)
