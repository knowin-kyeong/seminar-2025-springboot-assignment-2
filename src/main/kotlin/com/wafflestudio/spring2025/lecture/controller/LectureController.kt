package com.wafflestudio.spring2025.lecture.controller

import com.wafflestudio.spring2025.lecture.dto.ListLectureRequest
import com.wafflestudio.spring2025.lecture.dto.ListLectureResponse
import com.wafflestudio.spring2025.lecture.service.LectureService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class LectureController(
    private val lectureService: LectureService,
) {
    @GetMapping("/api/v1/lectures")
    fun listFromKeyword(
        @RequestParam(name = "year", defaultValue = "2025") year: Int,
        @RequestParam(name = "semester", defaultValue = "2") semester: Int,
        @RequestParam(name = "query", required = false) keyword: String?,
        @RequestParam(name = "size", defaultValue = "20") limit: Int,
        @RequestParam(name = "cursor", required = false) nextId: Long?,
    ): ResponseEntity<ListLectureResponse> {
        val request =
            ListLectureRequest(
                year = year,
                semester = semester,
                inputKey = keyword,
                limit = limit,
                nextId = nextId,
            )
        return ResponseEntity.ok(lectureService.listFromKeyword(request))
    }
}
