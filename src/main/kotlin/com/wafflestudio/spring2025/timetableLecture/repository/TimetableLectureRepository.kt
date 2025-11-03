package com.wafflestudio.spring2025.timetableLecture.repository

import com.wafflestudio.spring2025.timetableLecture.model.TimetableLecture
import org.springframework.data.repository.ListCrudRepository

interface TimetableLectureRepository : ListCrudRepository<TimetableLecture, Long> {
    fun existsByTimetableIdAndLectureId(
        timetableId: Long,
        lectureId: Long,
    ): Boolean

    fun findByTimetableId(
        timetableId: Long,
    ): List<TimetableLecture>

    fun deleteByTimetableIdAndLectureId(
        timetableId: Long,
        lectureId: Long
    )
}