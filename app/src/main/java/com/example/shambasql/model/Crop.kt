package com.example.shambasql.model

import java.util.Date

data class Crop(
    val id: Long = 0,
    val fieldId: Long,
    val name: String,
    val type: String,
    val season: String,
    val startDate: Date,
    val endDate: Date,
    val isActive: Boolean
)
