package com.wafflestudio.spring2025.timetableLecture

import com.wafflestudio.spring2025.DomainException
import com.wafflestudio.spring2025.lecture.LectureNotFoundException
import com.wafflestudio.spring2025.timetable.TimetableNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class TimetableLectureException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

typealias InvalidTimetableException = TimetableNotFoundException

typealias InvalidLectureIdException = LectureNotFoundException

class TimetableAccessForbiddenException :
    TimetableLectureException(
        errorCode = 0,
        httpStatusCode = HttpStatus.FORBIDDEN,
        msg = "You don't have permission to access this Timetable",
    )

class LectureTimeConflictException :
    TimetableLectureException(
        errorCode = 0,
        httpStatusCode = HttpStatus.CONFLICT,
        msg = "Lecture time conflict",
    )

class MatchFailedException :
    TimetableLectureException(
        errorCode = 0,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "Matching (timetable, lecture) not found",
    )
