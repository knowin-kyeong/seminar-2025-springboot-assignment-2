package com.wafflestudio.spring2025.lecture.util

object SugangSnuUrlUtils {
    fun convertSemesterToSugangSnuSearchString(semester: Int): String =
        when (semester) {
            1 -> "U000200001U000300001" // SPRING
            2 -> "U000200002U000300001" // FALL
            3 -> "U000200001U000300002" // SUMMER
            4 -> "U000200002U000300002" // WINTER
            else -> throw IndexOutOfBoundsException("Semester $semester not found")
        }
}
