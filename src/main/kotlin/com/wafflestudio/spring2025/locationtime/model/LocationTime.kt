package com.wafflestudio.spring2025.locationtime.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("locationtimes")
class LocationTime(
    @Id val id: Long? = null,
    val lectureId: Long,
    val dayOfWeek: Int,
    val startTime: Int,
    val endTime: Int,
    val location: String? = null,
)
