package com.wafflestudio.spring2025.timetableLecture.dto

import com.wafflestudio.spring2025.lecture.dto.LectureAndLocationTimeDto
import com.wafflestudio.spring2025.lecture.dto.LectureDto
import com.wafflestudio.spring2025.locationtime.dto.LocationTimeDto
import com.wafflestudio.spring2025.timetable.model.Timetable


data class ListTimetableLectureResponse(
    val timetable: Timetable,
    val credits: Int,
    val lectureAndLocationTimeDtos: List<LectureAndLocationTimeDto>
)
