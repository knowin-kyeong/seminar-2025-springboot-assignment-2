package com.wafflestudio.spring2025.lecture.service

import com.wafflestudio.spring2025.lecture.dto.ListLectureRequest
import com.wafflestudio.spring2025.lecture.dto.ListLectureResponse
import com.wafflestudio.spring2025.lecture.repository.LectureRepository
import org.springframework.stereotype.Service

@Service
class LectureService(
    private val lectureRepository: LectureRepository,
) {
    fun listFromKeyword(request: ListLectureRequest): ListLectureResponse {
        // TODO
        return TODO("아직 구현되지 않음")
    }
}
