package com.wafflestudio.spring2025.locationtime.repository

import com.wafflestudio.spring2025.locationtime.model.LocationTime
import org.springframework.data.repository.ListCrudRepository

interface LocationTimeRepository : ListCrudRepository<LocationTime, Long> {
    fun findByLectureId(lectureId: Long): List<LocationTime>
    fun findAllByLectureIdIn(lectureIds: List<Long>): List<LocationTime>
}