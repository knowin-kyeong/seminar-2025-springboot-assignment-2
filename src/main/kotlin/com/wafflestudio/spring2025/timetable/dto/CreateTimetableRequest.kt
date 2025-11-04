package com.wafflestudio.spring2025.timetable.dto

data class CreateTimetableRequest(
    val name: String,
    val year: Int,
    val semester: Int,
)
