package com.wafflestudio.spring2025.lecture.dto

import com.wafflestudio.spring2025.lecture.model.Lecture
import com.wafflestudio.spring2025.locationtime.model.LocationTime

data class LectureDto(
    val id: Long?,
    val year: Int,
    val semester: String,
    val lectureNumber: String,
    val classNumber: String,
    val title: String,
    val subtitle: String?,
    val credit: Int,
)

data class LectureAndLocationTimeDto(
    val lecture: Lecture,
    val locationTimes: List<LocationTime>,
)
