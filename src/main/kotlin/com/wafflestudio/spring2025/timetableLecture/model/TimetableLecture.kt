package com.wafflestudio.spring2025.timetableLecture.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("timetable_lecture")
class TimetableLecture(
    @Id var id: Long? = null,
    var timetableId: Long,
    var lectureId: Long,
)
