package com.wafflestudio.spring2025.timetableLecture.dto.core

import com.wafflestudio.spring2025.timetableLecture.model.TimetableLecture

data class TimetableLectureDto(
    val id: Long?,
    val timetableId: Long,
    val lectureId: Long,
) {
    constructor (timetableLecture: TimetableLecture) : this(
        id = timetableLecture.id,
        timetableId = timetableLecture.timetableId,
        lectureId = timetableLecture.lectureId,
    )
}
