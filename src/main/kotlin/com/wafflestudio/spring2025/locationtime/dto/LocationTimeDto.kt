package com.wafflestudio.spring2025.locationtime.dto

data class LocationTimeDto(
    val id: Long?,
    val dayOfWeek: Int,
    val startTime: Int,
    val endTime: Int,
    val location: String,
)
