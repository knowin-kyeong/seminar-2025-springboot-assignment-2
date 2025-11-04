package com.wafflestudio.spring2025.lecture.service

import com.wafflestudio.spring2025.lecture.dto.LectureDto
import com.wafflestudio.spring2025.lecture.dto.ListLectureRequest
import com.wafflestudio.spring2025.lecture.dto.ListLectureResponse
import com.wafflestudio.spring2025.lecture.dto.Paging
import com.wafflestudio.spring2025.lecture.repository.LectureRepository
import org.springframework.stereotype.Service

@Service
class LectureService(
    private val lectureRepository: LectureRepository,
) {
    fun listFromKeyword(request: ListLectureRequest): ListLectureResponse {
        val queryLimit = request.limit + 1
        val lectures =
            lectureRepository.findByYearAndSemesterWithCursor(
                year = request.year,
                semester = request.semester,
                keyword = request.inputKey,
                nextId = request.nextId,
                limit = queryLimit,
            )
        val hasNext = lectures.size > request.limit
        val data = if (hasNext) lectures.subList(0, request.limit) else lectures
        val nextCursor = if (hasNext) data.last().id else null

        return ListLectureResponse(
            data.map { LectureDto(it) },
            Paging(hasNext, nextCursor),
        )
    }
}
