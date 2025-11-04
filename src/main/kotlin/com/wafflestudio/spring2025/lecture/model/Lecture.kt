package com.wafflestudio.spring2025.lecture.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("lectures")
class Lecture(
    @Id val id: Long? = null,
    val year: Int,
    val semester: Int,
    val lectureNumber: String, // 교과목 번호
    val classNumber: String, // 수강분반
    val title: String, // 제목
    val subtitle: String? = null, // 부제목
    val credit: Int,
    // 추가 스펙, snutt참고: https://github.com/wafflestudio/snutt/blob/ef1c0d965b3e9b3847105dcf775adf8886f1f0d3/batch/src/main/kotlin/sugangsnu/common/service/SugangSnuFetchService.kt#L13
    val classification: String, // 교과구분
    val college: String, // 개설대학
    val department: String, // 개설학과
    val academicCourse: String, // 이수 과정
    val academicYear: String, // 학년
    val instructor: String, // 담당교수
)
