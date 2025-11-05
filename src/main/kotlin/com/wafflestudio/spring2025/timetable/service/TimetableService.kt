package com.wafflestudio.spring2025.timetable.service

import com.wafflestudio.spring2025.timetable.TimetableBlankNameException
import com.wafflestudio.spring2025.timetable.TimetableNameConflictException
import com.wafflestudio.spring2025.timetable.TimetableNotFoundException
import com.wafflestudio.spring2025.timetable.TimetableUpdateForbiddenException
import com.wafflestudio.spring2025.timetable.dto.CreateTimetableResponse
import com.wafflestudio.spring2025.timetable.dto.ListTimetableResponse
import com.wafflestudio.spring2025.timetable.dto.core.TimetableDto
import com.wafflestudio.spring2025.timetable.model.Timetable
import com.wafflestudio.spring2025.timetable.repository.TimetableRepository
import com.wafflestudio.spring2025.user.model.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TimetableService(
    private val timetableRepository: TimetableRepository,
) {
    fun create(
        name: String,
        user: User,
        year: Int,
        semester: Int,
    ): CreateTimetableResponse {
        if (name.isBlank()) {
            throw TimetableBlankNameException()
        }
        if (timetableRepository.existsByUserIdAndNameAndYearAndSemester(user.id!!, name, year, semester)) {
            throw TimetableNameConflictException()
        }

        val timetable =
            timetableRepository.save(
                Timetable(
                    name = name,
                    year = year,
                    semester = semester,
                    userId = user.id!!,
                ),
            )
        return TimetableDto(timetable)
    }

    fun list(user: User): ListTimetableResponse {
        val timetables = timetableRepository.findAllByUserId(user.id!!)
        return timetables.map { TimetableDto(it) }
    }

    fun update(
        timetableId: Long,
        name: String?,
        user: User,
    ): TimetableDto {
        if (name?.isBlank() == true) {
            throw TimetableBlankNameException()
        }

        val timetable =
            timetableRepository.findByIdOrNull(timetableId)
                ?: throw TimetableNotFoundException()

        if (timetable.userId != user.id) {
            throw TimetableUpdateForbiddenException()
        }

        if (
            name != null &&
            name != timetable.name &&
            timetableRepository.existsByUserIdAndNameAndYearAndSemester(timetable.userId, name, timetable.year, timetable.semester)
        ) {
            throw TimetableNameConflictException()
        }

        name?.let { timetable.name = it }
        timetableRepository.save(timetable)

        val updated =
            timetableRepository.findByIdOrNull(timetable.id!!)
                ?: throw TimetableNotFoundException()
        return TimetableDto(updated)
    }

    fun delete(
        timetableId: Long,
        user: User,
    ) {
        val timetable =
            timetableRepository.findByIdOrNull(timetableId)
                ?: throw TimetableNotFoundException()

        if (timetable.userId != user.id) {
            throw TimetableUpdateForbiddenException()
        }

        timetableRepository.delete(timetable)
    }

    fun detail(
        timetableId: Long,
        user: User,
    ): TimetableDto {
        val timetable = timetableRepository.findByIdOrNull(timetableId) ?: throw TimetableNotFoundException()

        if (timetable.userId != user.id) {
            throw TimetableUpdateForbiddenException()
        }

        return TimetableDto(timetable)
    }
}
