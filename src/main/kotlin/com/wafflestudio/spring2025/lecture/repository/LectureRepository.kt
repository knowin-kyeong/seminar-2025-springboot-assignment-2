package com.wafflestudio.spring2025.lecture.repository

import com.wafflestudio.spring2025.lecture.model.Lecture
import org.springframework.data.repository.ListCrudRepository

interface LectureRepository : ListCrudRepository<Lecture, Long> {
    // TODO
    // 페이지네이션 구현
    /* keyword query가 주어질 때 해당하는 강좌 List<Lecture>을 반환하도록 해야 함 */
    //시간 중복 검증용. 시간표에 담긴 강의 상세 정보 조회용
    fun findAllByIdIn(ids: List<Long>): List<Lecture>

    //강의 검색 기능, 연도 학기 키워드 기준 검색
    fun findAllByYearAndSemesterAndTitleContainingOrInstructorContaining(
        year: Int,
        semester: Int,
        titleKeyword: String,
        instructorKeyword: String
    ): List<Lecture>





}