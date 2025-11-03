package com.wafflestudio.spring2025.lecture.repository

import com.wafflestudio.spring2025.lecture.model.Lecture
import org.springframework.data.repository.ListCrudRepository

interface LectureRepository : ListCrudRepository<Lecture, Long> {
    // TODO
    /* keyword query가 주어질 때 해당하는 강좌 List<Lecture>을 반환하도록 해야 함 */
}