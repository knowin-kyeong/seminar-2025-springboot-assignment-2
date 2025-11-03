package com.wafflestudio.spring2025.timetableLecture.controller

import com.wafflestudio.spring2025.timetable.dto.CreateTimetableRequest
import com.wafflestudio.spring2025.timetable.dto.CreateTimetableResponse
import com.wafflestudio.spring2025.timetable.dto.ListTimetableResponse
import com.wafflestudio.spring2025.timetable.dto.UpdateTimetableRequest
import com.wafflestudio.spring2025.timetable.dto.core.TimetableDto
import com.wafflestudio.spring2025.timetable.service.TimetableService
import com.wafflestudio.spring2025.timetableLecture.dto.CreateTimetableLectureRequest
import com.wafflestudio.spring2025.timetableLecture.dto.CreateTimetableLectureResponse
import com.wafflestudio.spring2025.timetableLecture.service.TimetableLectureService
import com.wafflestudio.spring2025.user.LoggedInUser
import com.wafflestudio.spring2025.user.model.User
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TimetableLectureController(
    private val timetableLectureService: TimetableLectureService,
) {
    @PostMapping("/api/v1/timetables/{id}/lectures")
    fun create(
        @PathVariable id: Long,
        @RequestBody createRequest: CreateTimetableLectureRequest,
        @LoggedInUser user: User,
    ): ResponseEntity<CreateTimetableLectureResponse> {
        val timetableLecture =
            timetableLectureService.create(
                timetableId = id,
                lectureId = createRequest.lectureId,
                user = user
            )
        return ResponseEntity.ok(timetableLecture)
    }

    @DeleteMapping("/api/v1/timetables/{id}/lectures/{lectureId}")
    fun delete(
        @PathVariable id: Long,
        @PathVariable lectureId: Long,
        @LoggedInUser user: User
    ): ResponseEntity<Unit> {
        timetableLectureService.delete(
            timetableId = id,
            lectureId = lectureId,
            user = user
        )
        return ResponseEntity.noContent().build()
    }
}
