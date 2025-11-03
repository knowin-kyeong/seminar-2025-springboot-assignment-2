package com.wafflestudio.spring2025.timetableLecture.service

import com.wafflestudio.spring2025.lecture.LectureNotFoundException
import com.wafflestudio.spring2025.lecture.dto.LectureAndLocationTimeDto
import com.wafflestudio.spring2025.lecture.dto.LectureDto
import com.wafflestudio.spring2025.lecture.repository.LectureRepository
import com.wafflestudio.spring2025.locationtime.repository.LocationTimeRepository
import com.wafflestudio.spring2025.timetable.TimetableNotFoundException
import com.wafflestudio.spring2025.timetable.dto.core.TimetableDto
import com.wafflestudio.spring2025.timetable.repository.TimetableRepository
import com.wafflestudio.spring2025.timetableLecture.MatchFailedException
import com.wafflestudio.spring2025.timetableLecture.TimetableAccessForbiddenException
import com.wafflestudio.spring2025.timetableLecture.dto.CreateTimetableLectureResponse
import com.wafflestudio.spring2025.timetableLecture.dto.ListTimetableLectureResponse
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
    private val lectureRepository: LectureRepository,
    private val locationTimeRepository: LocationTimeRepository
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

    fun listLectures(
            timetableId: Long,
            user: User,
        ): ListTimetableLectureResponse {
        val timetable = timetableRepository.findByIdOrNull(timetableId) ?: throw TimetableNotFoundException()
        if (timetable.userId != user.id) {
            throw TimetableAccessForbiddenException()
        }

        val lectureIds = timetableLectureRepository.findByTimetableId(timetableId).map{it.lectureId}
        if (lectureIds.isEmpty()) {
            return ListTimetableLectureResponse(
                timetable = timetable,
                credits = 0,
                lectureAndLocationTimeDtos = emptyList<LectureAndLocationTimeDto>()
            )
        }

        val lectures = lectureIds.map{lectureRepository.findByIdOrNull(it)}
        val locationTimes = lectureIds.map{locationTimeRepository.findByLectureId(it)}

        val lectureAndLocationTimes = mutableListOf<LectureAndLocationTimeDto>()
        for(i in lectureIds.indices){
            val currentLecture = lectures[i]!!
            val currentLocationTimes = locationTimes[i]
            val lectureAndLocationTimeDto = LectureAndLocationTimeDto(
                lecture = currentLecture,
                locationTimes = currentLocationTimes
            )

            lectureAndLocationTimes.add(lectureAndLocationTimeDto)
        }

        val credits = lectures.sumOf { it?.credit ?: 0 }

        return ListTimetableLectureResponse(
            timetable = timetable,
            credits = credits,
            lectureAndLocationTimeDtos = lectureAndLocationTimes.toList()
        )
    }

    fun delete(
        timetableId: Long,
        lectureId: Long,
        user: User,
    ): Unit {
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
