package com.wafflestudio.spring2025.lecture.dto

data class ListLectureRequest(
    val year: Int,
    val semester: Int,
    val inputKey: String? = null,
    val limit: Int = 10,
    val nextId: Long? = null,
)
