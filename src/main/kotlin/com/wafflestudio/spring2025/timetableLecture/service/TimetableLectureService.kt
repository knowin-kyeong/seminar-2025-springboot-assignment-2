package com.wafflestudio.spring2025.timetableLecture.service

import com.wafflestudio.spring2025.timetable.TimetableNotFoundException
import com.wafflestudio.spring2025.timetable.dto.core.TimetableDto
import com.wafflestudio.spring2025.timetable.repository.TimetableRepository
import com.wafflestudio.spring2025.timetableLecture.MatchFailedException
import com.wafflestudio.spring2025.timetableLecture.TimetableAccessForbiddenException
import com.wafflestudio.spring2025.timetableLecture.dto.CreateTimetableLectureResponse
import com.wafflestudio.spring2025.timetableLecture.dto.core.TimetableLectureDto
import com.wafflestudio.spring2025.timetableLecture.model.TimetableLecture
import com.wafflestudio.spring2025.timetableLecture.repository.TimetableLectureRepository
import com.wafflestudio.spring2025.user.model.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TimetableLectureService(
    private val timetableLectureRepository: TimetableLectureRepository,
    private val timetableRepository: TimetableRepository,
    private val lectureRepository: LectureRepository
) {
    fun create(
        timetableId: Long,
        lectureId: Long,
        user: User,
    ): CreateTimetableLectureResponse {

        val timetable = timetableRepository.findByIdOrNull(timetableId) ?: throw TimetableNotFoundException()
        if (timetable.userId != user.id) {
            throw TimetableAccessForbiddenException()
        }

        val lecture = lectureRepository.findByIdOrNull(lectureId) ?: throw LectureNotFoundException()

        //TODO: lecture time conflict check

        val timetableLecture =
            timetableLectureRepository.save(
            TimetableLecture(
                timetableId = timetableId,
                lectureId = lectureId
            )
        )

        return TimetableLectureDto(
            id = timetableLecture.id,
            timetableId = timetableId,
            lectureId = lectureId
        )

    }

    fun delete(
        timetableId: Long,
        lectureId: Long,
        user: User,
    ): TimetableDto {
        val timetable = timetableRepository.findByIdOrNull(timetableId) ?: throw TimetableNotFoundException()
        if (timetable.userId != user.id) {
            throw TimetableAccessForbiddenException()
        }

        val lecture = lectureRepository.findByIdOrNull(lectureId) ?: throw LectureNotFoundException()

        if (!timetableLectureRepository.existsByTimetableIdAndLectureId(timetableId, lectureId)) {
            throw MatchFailedException()
        }

        timetableLectureRepository.deleteByTimetableIdAndLectureId(timetableId, lectureId)
    }

}
