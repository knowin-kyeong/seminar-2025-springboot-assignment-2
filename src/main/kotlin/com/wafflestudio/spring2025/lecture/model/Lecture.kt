package com.wafflestudio.spring2025.lecture.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("lectures")
class Lecture(
    @Id val id: Long? = null,
    val year: Int,
    val semester: Int,
    val lectureNumber: String, // 교과목 번호
    val classNumber: String,   // 수강분반
    val title: String,         // 제목
    val subtitle: String? = null,     // 부제목
    val credit: Int,
)
