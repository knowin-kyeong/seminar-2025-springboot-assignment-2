package com.wafflestudio.spring2025

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.spring2025.helper.DataGenerator
import com.wafflestudio.spring2025.lecture.repository.LectureRepository
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.stereotype.Repository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.junit.jupiter.Testcontainers

import com.wafflestudio.spring2025.timetable.repository.TimetableRepository
import com.wafflestudio.spring2025.timetable.dto.CreateTimetableRequest
import com.wafflestudio.spring2025.timetable.dto.UpdateTimetableRequest
import com.wafflestudio.spring2025.timetableLecture.dto.CreateTimetableLectureRequest

import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.http.MediaType
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.hasSize


@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class TimetableIntegrationTest
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val dataGenerator: DataGenerator,
        private val timetableRepository: TimetableRepository,
        private val lectureRepository: LectureRepository,
    ) {
        @Test
        fun `should create a timetable`() {
            // 시간표를 생성할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val request = CreateTimetableRequest(
                name = "테스트용 시간표",
                year = 2025,
                semester = 2,
            )

            mvc
                .perform(
                post("/api/v1/timetables")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("테스트용 시간표"))
            .andExpect(jsonPath("$.semester").value(2))
            .andExpect(jsonPath("$.year").value(2025))
        }

        @Test
        fun `should retrieve all own timetables`() {
            // 자신의 모든 시간표 목록을 조회할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable1 = dataGenerator.generateTimetable(user=user, name="Timetable 1")
            val timetable2 = dataGenerator.generateTimetable(user=user, name="Timetable 2")

            mvc
                .perform(
                    get("/api/v1/timetables")
                        .header("Authorization", "Bearer $token")
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Timetable 1"))
                .andExpect(jsonPath("$[1].name").value("Timetable 2"))
        }

        @Test
        fun `should retrieve timetable details`() {
            // 시간표 상세 정보를 조회할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(user=user, name="Timetable 1")
            val lecture = dataGenerator.generateLectureandLocationTime(credit = 2)
            dataGenerator.connectTimetableWithLecture(timetable, lecture)

            mvc.perform(
                get("/api/v1/timetables/{id}", timetable.id)
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.timetable.name").value(timetable.name))
                .andExpect(jsonPath("$.timetable.year").value(timetable.year))
                .andExpect(jsonPath("$.timetable.semester").value(timetable.semester.toString())) // .toString()은 'semester'가 Enum인 경우를 대비
                .andExpect(jsonPath("$.credits").value(2))
                .andExpect(jsonPath("$.lectureAndLocationTimeDtos.length()").value(1))
                .andExpect(jsonPath("$.lectureAndLocationTimeDtos[0].lecture.title").value(lecture.title))
                .andExpect(jsonPath("$.lectureAndLocationTimeDtos[0].lecture.credit").value(2))
        }

        @Test
        fun `should update timetable name`() {
            // 시간표 이름을 수정할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(user = user, name = "Before")

            val request = UpdateTimetableRequest(name = "After")
            
            mvc
                .perform(
                    patch("/api/v1/timetables/${timetable.id!!}")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(timetable.id!!))
                .andExpect(jsonPath("$.name").value("After"))
        }

        @Test
        fun `should not update another user's timetable`() {
            // 다른 사람의 시간표는 수정할 수 없다
            val (user1, token1) = dataGenerator.generateUser()
            val (user2, token2) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(user = user1, name = "User1 Timetable")

            val request = UpdateTimetableRequest(name = "User2's Name")

            mvc
                .perform(
                    patch("/api/v1/timetables/${timetable.id!!}")
                        .header("Authorization", "Bearer $token2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isForbidden)

        }

        @Test
        fun `should delete a timetable`() {
            // 시간표를 삭제할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(user = user, name = "To Delete")

            // when & then
            mvc
                .perform(
                    delete("/api/v1/timetables/${timetable.id!!}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isNoContent())

            assert(timetableRepository.findById(timetable.id!!).isEmpty)
        }

        @Test
        fun `should not delete another user's timetable`() {
            // 다른 사람의 시간표는 삭제할 수 없다
            val (user1, token1) = dataGenerator.generateUser()
            val (user2, token2) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(user = user1, name = "User1 Timetable")

            // when & then
            mvc
                .perform(
                    delete("/api/v1/timetables/${timetable.id!!}")
                        .header("Authorization", "Bearer $token2"),
                ).andExpect(status().isForbidden)
        }

        @Test
        @Disabled("TODO")
        fun `should search for courses`() {
            // 강의를 검색할 수 있다
        }

        @Test
        fun `should add a course to timetable`() {
            // 시간표에 강의를 추가할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(user = user)
            val lecture = dataGenerator.generateLectureandLocationTime()

            val request = CreateTimetableLectureRequest(
                lectureId = lecture.id!!
            )

            mvc.perform(
                post("/api/v1/timetables/${timetable.id!!}/lectures")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.timetableId").value(timetable.id!!))
                .andExpect(jsonPath("$.lectureId").value(lecture.id))
        }

    @Test
    fun `should return error when adding overlapping course to timetable`() {
        val (user, token) = dataGenerator.generateUser()
        val timetable = dataGenerator.generateTimetable(user = user, year = 2025, semester = 2)

        // 강의 A: 월 10:00 ~ 11:30
        val lectureA = dataGenerator.generateLectureandLocationTime(
            title = "기존 강의 A",
            dayofWeek = 1,
            startTime = 1000,
            endTime = 1130
        )
        dataGenerator.connectTimetableWithLecture(timetable, lectureA)

        // 강의 B: 월 10:30 ~ 12:00 (겹침)
        val lectureB = dataGenerator.generateLectureandLocationTime(
            title = "겹치는 강의 B",
            dayofWeek = 1,
            startTime = 1030,
            endTime = 1200
        )

        val request = CreateTimetableLectureRequest(
            lectureId = lectureB.id!!
        )

        // 강의 B 추가 시도 시 409 CONFLICT 에러 검증
        mvc.perform(
            post("/api/v1/timetables/${timetable.id!!}/lectures")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)),
        ).andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("Lecture time conflict"))
    }
        @Test
        fun `should not add a course to another user's timetable`() {
            // 다른 사람의 시간표에는 강의를 추가할 수 없다
            val (user1, token1) = dataGenerator.generateUser()
            val (user2, token2) = dataGenerator.generateUser()

            val timetable1 = dataGenerator.generateTimetable(user = user1)
            val lecture = dataGenerator.generateLectureandLocationTime()

            val request = CreateTimetableLectureRequest(
                lectureId = lecture.id!!
            )

            mvc.perform(
                post("/api/v1/timetables/${timetable1.id!!}/lectures")
                    .header("Authorization", "Bearer $token2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)),
            ).andExpect(status().isForbidden)
        }

        @Test
        fun `should remove a course from timetable`() {
            // 시간표에서 강의를 삭제할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(user = user)
            val lecture = dataGenerator.generateLectureandLocationTime()

            val connection = dataGenerator.connectTimetableWithLecture(timetable, lecture)

            mvc.perform (
                delete("/api/v1/timetables/${timetable.id!!}/lectures/${lecture.id}")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isNoContent)
        }

        @Test
        fun `should not remove a course from another user's timetable`() {
            // 다른 사람의 시간표에서는 강의를 삭제할 수 없다
            val (user1, token1) = dataGenerator.generateUser()
            val (user2, token2) = dataGenerator.generateUser()

            val timetable1 = dataGenerator.generateTimetable(user = user1)
            val lecture = dataGenerator.generateLectureandLocationTime()

            val connection = dataGenerator.connectTimetableWithLecture(timetable1, lecture)

            mvc.perform (
                delete("/api/v1/timetables/${timetable1.id!!}/lectures/${lecture.id}")
                    .header("Authorization", "Bearer $token2")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isForbidden)
        }

        @Test
        @Disabled("곧 안내드리겠습니다")
        fun `should fetch and save course information from SNU course registration site`() {
            // 서울대 수강신청 사이트에서 강의 정보를 가져와 저장할 수 있다
        }

        @Test
        @Disabled("TODO")
        fun `should return correct course list and total credits when retrieving timetable details`() {
            // 시간표 상세 조회 시, 강의 정보 목록과 총 학점이 올바르게 반환된다
        }

        @Test
        @Disabled("TODO")
        fun `should paginate correctly when searching for courses`() {
            // 강의 검색 시, 페이지네이션이 올바르게 동작한다
        }
    }
