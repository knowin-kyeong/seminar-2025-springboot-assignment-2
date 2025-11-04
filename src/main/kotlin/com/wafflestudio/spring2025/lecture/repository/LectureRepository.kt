package com.wafflestudio.spring2025.lecture.repository

import com.wafflestudio.spring2025.lecture.model.Lecture
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param

interface LectureRepository : ListCrudRepository<Lecture, Long> {
    // 시간 중복 검증용. 시간표에 담긴 강의 상세 정보 조회용 */
    fun findAllByIdIn(ids: List<Long>): List<Lecture>

    // 강의 검색 기능, 연도 학기 키워드 기준 검색
    fun findAllByYearAndSemesterAndTitleContainingOrInstructorContaining(
        year: Int,
        semester: Int,
        titleKeyword: String,
        instructorKeyword: String,
    ): List<Lecture>

    @Query(
        """
        SELECT *
        FROM lectures l
        WHERE l.year = :year 
            AND l.semester = :semester
            AND (:keyword IS NULL 
                OR l.title LIKE CONCAT('%', :keyword, '%') 
                OR l.instructor LIKE CONCAT('%', :keyword, '%'))
            AND (:nextId IS NULL OR l.id > :nextId)
        LIMIT :limit
    """,
    )
    fun findByYearAndSemesterWithCursor(
        @Param("year") year: Int,
        @Param("semester") semester: Int,
        @Param("keyword") keyword: String?,
        @Param("nextId") nextId: Long?,
        @Param("limit") limit: Int,
    ): List<Lecture>
}
