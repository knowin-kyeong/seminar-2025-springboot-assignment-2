package com.wafflestudio.spring2025.timetableLecture.dto

data class CreateTimetableLectureRequest(
    val id: Long? = null,
    val timetableId: Long,
    val lectureId: Long
)

