package com.example.shambasql.model

import java.util.Date

data class CropHarvest(
    val id: Long = 0,
    val fieldId: Long,
    val cropId: Long,
    val unitType: String,        // e.g. "kg", "ton", "bags"
    val quantity: Double,        // Quantity harvested
    val valuePerUnit: Double,    // Price per unit (e.g. per kg)
    val totalValue: Double,      // Calculated: quantity × valuePerUnit
    val harvestDate: Date,       // Date of harvest
    val notes: String = ""       // Optional notes
)

