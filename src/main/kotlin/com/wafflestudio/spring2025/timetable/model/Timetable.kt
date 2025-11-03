package com.wafflestudio.spring2025.timetable.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("timetables")
class Timetable(
    @Id var id: Long? = null,
    var name: String,
    var year: Int,
    var semester: Int,
    var userId: Long,
)
