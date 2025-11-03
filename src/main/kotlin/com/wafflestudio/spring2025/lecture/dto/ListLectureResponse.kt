package com.wafflestudio.spring2025.lecture.dto

data class ListLectureResponse(
    val data: List<LectureDto>,
    val paging: Paging,
)

data class Paging(
    val hasNext: Boolean,
    val nextCursor: Long?,
)
