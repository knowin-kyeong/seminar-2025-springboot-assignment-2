package com.wafflestudio.spring2025.locationtime.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.DayOfWeek
import java.time.LocalTime

@Table("locationtimes")
class LocationTime(
    @Id val id: Long,
    val lectureId: Long,
    val dayOfWeek: Int,
    val startTime: Int,
    val endTime: Int,
    val location: String,
)