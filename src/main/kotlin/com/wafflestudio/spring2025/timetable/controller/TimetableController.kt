package com.wafflestudio.spring2025.timetable.controller

import com.wafflestudio.spring2025.timetable.dto.CreateTimetableRequest
import com.wafflestudio.spring2025.timetable.dto.CreateTimetableResponse
import com.wafflestudio.spring2025.timetable.dto.ListTimetableResponse
import com.wafflestudio.spring2025.timetable.dto.UpdateTimetableRequest
import com.wafflestudio.spring2025.timetable.dto.core.TimetableDto
import com.wafflestudio.spring2025.timetable.service.TimetableService
import com.wafflestudio.spring2025.user.LoggedInUser
import com.wafflestudio.spring2025.user.model.User
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/timetables")
class TimetableController(
    private val timetableService: TimetableService,
) {
    @PostMapping
    fun create(
        @RequestBody createRequest: CreateTimetableRequest,
        @LoggedInUser user: User,
    ): ResponseEntity<CreateTimetableResponse> {
        val timetable =
            timetableService.create(
                name = createRequest.name,
                year = createRequest.year,
                semester = createRequest.semester,
                user = user,
            )
        return ResponseEntity.ok(timetable)
    }

    @GetMapping
    fun list(
        @LoggedInUser user: User,
    ): ResponseEntity<ListTimetableResponse> {
        val timetables = timetableService.list(user)
        return ResponseEntity.ok(timetables)
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @LoggedInUser user: User,
        @RequestBody updateRequest: UpdateTimetableRequest,
    ): ResponseEntity<TimetableDto> {
        val timetable =
            timetableService.update(
                timetableId = id,
                name = updateRequest.name,
                user = user,
            )
        return ResponseEntity.ok(timetable)
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        @LoggedInUser user: User,
    ): ResponseEntity<Unit> {
        timetableService.delete(id, user)
        return ResponseEntity.noContent().build()
    }

}
