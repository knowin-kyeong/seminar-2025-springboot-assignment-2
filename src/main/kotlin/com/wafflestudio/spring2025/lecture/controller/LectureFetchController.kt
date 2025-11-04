package com.wafflestudio.spring2025.lecture.controller

import com.wafflestudio.spring2025.lecture.service.SugangSnuLectureSyncService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class LectureFetchController(
    private val sugangSnuLectureSyncService: SugangSnuLectureSyncService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * SNU 수강 사이트로부터 강의 정보를 가져와 DB에 동기화합니다.
     * @param year 연도 (예: 2024)
     * @param semester 학기 (예: SPRING, SUMMER, AUTUMN, WINTER)
     */
    @PostMapping("/api/v1/lectures/fetch")
    fun fetchLectures(
        @RequestParam("year") year: Int,
        @RequestParam("semester") semester: Int,
    ): ResponseEntity<Map<String, String>> {
        log.info("Lecture fetch request received: year=$year, semester=$semester")
        return try {
            // syncLectures는 suspend 함수이므로 컨트롤러도 suspend로 선언해야 합니다.
            sugangSnuLectureSyncService.syncLectures(year, semester)

            val successMessage = "Lecture sync successful for $year $semester"
            log.info(successMessage)
            ResponseEntity.ok(mapOf("message" to successMessage))
        } catch (e: Exception) {
            log.error("Lecture sync failed for $year $semester", e)
            ResponseEntity
                .internalServerError()
                .body(mapOf("error" to "Sync failed: ${e.message}"))
        }
    }
}
