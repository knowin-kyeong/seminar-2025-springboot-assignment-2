package com.wafflestudio.spring2025.timetable.repository

import com.wafflestudio.spring2025.timetable.model.Timetable
import org.springframework.data.repository.ListCrudRepository

interface TimetableRepository : ListCrudRepository<Timetable, Long> {
    fun existsByUserIdAndNameAndYearAndSemester(
        userId: Long,
        name: String,
        year: Int,
        semester: Int,
    ): Boolean

    fun findAllByUserId(userId: Long): List<Timetable>
}
