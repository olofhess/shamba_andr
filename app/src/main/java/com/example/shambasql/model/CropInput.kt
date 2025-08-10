package com.example.shambasql.model

import java.util.*
import com.example.shambasql.model.CropInput


data class CropInput(
    val id: Long = 0,
    val fieldId: Long,
    val cropId: Long,
    val name: String,
    val inputType: String,     // e.g. "Fertilizer", "Pesticide", "Seed"
    val unit: String,          // e.g. "kg", "liter"
    val quantity: Double,
    val costPerUnit: Double,
    val totalCost: Double,
    val date: Date,
    val notes: String = ""
)
