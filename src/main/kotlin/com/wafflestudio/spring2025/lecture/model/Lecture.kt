package com.wafflestudio.spring2025.lecture.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("lectures")
class Lecture(
    @Id val id: Long,
    val year: Int,
    val semester: String,
    val lectureNumber: String, // 교과목 번호
    val classNumber: String,   // 수강분반
    val title: String,         // 제목
    val subtitle: String?,     // 부제목
    val credit: Int,
)
