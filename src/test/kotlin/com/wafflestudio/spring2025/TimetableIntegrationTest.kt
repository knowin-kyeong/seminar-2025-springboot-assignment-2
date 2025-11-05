package com.wafflestudio.spring2025

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.spring2025.helper.DataGenerator
import com.wafflestudio.spring2025.helper.QueryCounter
import com.wafflestudio.spring2025.lecture.dto.ListLectureResponse
import com.wafflestudio.spring2025.lecture.repository.LectureRepository
import com.wafflestudio.spring2025.locationtime.repository.LocationTimeRepository
import com.wafflestudio.spring2025.timetable.dto.CreateTimetableRequest
import com.wafflestudio.spring2025.timetableLecture.repository.TimetableLectureRepository
import org.junit.jupiter.api.Assertions.assertTrue
import com.wafflestudio.spring2025.timetable.dto.UpdateTimetableRequest
import com.wafflestudio.spring2025.timetable.repository.TimetableRepository
import com.wafflestudio.spring2025.timetableLecture.dto.CreateTimetableLectureRequest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
@Transactional
class TimetableIntegrationTest
@Autowired
constructor(
    private val mvc: MockMvc,
    private val mapper: ObjectMapper,
    private val dataGenerator: DataGenerator,
    private val queryCounter: QueryCounter,
    private val timetableRepository: TimetableRepository,
    private val lectureRepository: LectureRepository,
    private val locationTimeRepository: LocationTimeRepository,
    private val timetableLectureRepository: TimetableLectureRepository,
) {
    @Test
    fun `should create a timetable`() {
        // 시간표를 생성할 수 있다
        val (user, token) = dataGenerator.generateUser()
        val request =
            CreateTimetableRequest(
                name = "테스트용 시간표",
                year = 2025,
                semester = 2,
            )

        mvc
            .perform(
                post("/api/v1/timetables")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)),
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
        val timetable1 = dataGenerator.generateTimetable(user = user, name = "Timetable 1")
        val timetable2 = dataGenerator.generateTimetable(user = user, name = "Timetable 2")

        mvc
            .perform(
                get("/api/v1/timetables")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Timetable 1"))
            .andExpect(jsonPath("$[1].name").value("Timetable 2"))
    }

    @Test
    fun `should retrieve timetable details`() {
        // 시간표 상세 정보를 조회할 수 있다
        val (user, token) = dataGenerator.generateUser()
        val timetable = dataGenerator.generateTimetable(user = user, name = "Timetable 1")
        val lecture = dataGenerator.generateLectureandLocationTime(credit = 2)
        dataGenerator.connectTimetableWithLecture(timetable, lecture)

        mvc
            .perform(
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
    fun `should delete a timetable and its associated lectures`() {
        // 시간표를 삭제할 수 있다.
        val (user, token) = dataGenerator.generateUser()
        val timetable = dataGenerator.generateTimetable(user = user, name = "To Delete")
        val lecture = dataGenerator.generateLectureandLocationTime()
        val connection = dataGenerator.connectTimetableWithLecture(timetable, lecture)

        // when & then
        mvc
            .perform(
                delete("/api/v1/timetables/${timetable.id!!}")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isNoContent())

        assert(timetableRepository.findById(timetable.id!!).isEmpty)
        assert(timetableLectureRepository.findById(connection.id!!).isEmpty)
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
    fun `should search for courses by title`() {
        // 강의명으로 강의를 검색할 수 있다
        val (user, token) = dataGenerator.generateUser()

        repeat(5) {
            dataGenerator.generateLectureWithSemester(2025, 1)
            dataGenerator.generateLectureWithSemester(2025, 2)
            dataGenerator.generateLectureWithSemester(2024, 2)
        }
        val lecture = dataGenerator.generateLectureWithSemester(2025, 2)
        dataGenerator.generateLectureWithSemester(2024, 2, lecture.title)

        val response =
            mvc
                .perform(
                    get("/api/v1/lectures?year=2025&semester=2&query=${lecture.title}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)
                .andReturn()
                .response
                .getContentAsString(Charsets.UTF_8)
                .let {
                    mapper.readValue(it, ListLectureResponse::class.java)
                }

        assertTrue(response.data.size == 1 && response.data[0].id == lecture.id)
    }

    @Test
    fun `should search for courses by instructor name`() {
        //교수명으로 강의를 검색할 수 있다
        val (user, token) = dataGenerator.generateUser()

        repeat(5) {
            dataGenerator.generateLectureWithSemester(2025, 1)
            dataGenerator.generateLectureWithSemester(2025, 2)
            dataGenerator.generateLectureWithSemester(2024, 2)
        }

        val uniqueInstructorName = "TestInstructor-${kotlin.random.Random.nextInt(10000)}"

        val lecture = dataGenerator.generateLectureWithSemester(
            year = 2025,
            semester = 2,
            instructor = uniqueInstructorName
        )

        dataGenerator.generateLectureWithSemester(
            year = 2024,
            semester = 2,
            instructor = uniqueInstructorName
        )

        val response =
            mvc
                .perform(
                    get("/api/v1/lectures?year=2025&semester=2&query=$uniqueInstructorName")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)
                .andReturn()
                .response
                .getContentAsString(Charsets.UTF_8)
                .let {
                    mapper.readValue(it, ListLectureResponse::class.java)
                }

        assertTrue(response.data.size == 1 && response.data[0].id == lecture.id)
    }

    @Test
    fun `should add a course to timetable`() {
        // 시간표에 강의를 추가할 수 있다
        val (user, token) = dataGenerator.generateUser()
        val timetable = dataGenerator.generateTimetable(user = user)
        val lecture = dataGenerator.generateLectureandLocationTime()

        val request =
            CreateTimetableLectureRequest(
                lectureId = lecture.id!!,
            )
        queryCounter.assertQueryCount(6) {
            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id!!}/lectures")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.timetableId").value(timetable.id!!))
                .andExpect(jsonPath("$.lectureId").value(lecture.id))
        }
    }

    @Test
    fun `should return error when adding overlapping course to timetable`() {
        //겹치는 강의를 추가하면 에러를 반환한다.
        val (user, token) = dataGenerator.generateUser()
        val timetable = dataGenerator.generateTimetable(user = user, year = 2025, semester = 2)

        val lectureA =
            dataGenerator.generateLectureandLocationTime(
                title = "Base Lecture A",
                dayofWeek = 1,
                startTime = 1000,
                endTime = 1130,
            )
        dataGenerator.connectTimetableWithLecture(timetable, lectureA)

        val lectureB =
            dataGenerator.generateLectureandLocationTime(
                title = "Partially Overlapping Lecture B",
                dayofWeek = 1,
                startTime = 1030,
                endTime = 1200,
            )
        val requestB = CreateTimetableLectureRequest(lectureId = lectureB.id!!)

        mvc
            .perform(
                post("/api/v1/timetables/${timetable.id!!}/lectures")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(requestB)),
            ).andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("Lecture time conflict"))

        val lectureC =
            dataGenerator.generateLectureandLocationTime(
                title = "Fully Contained Lecture C",
                dayofWeek = 1,
                startTime = 1030,
                endTime = 1100,
            )
        val requestC = CreateTimetableLectureRequest(lectureId = lectureC.id!!)

        mvc
            .perform(
                post("/api/v1/timetables/${timetable.id!!}/lectures")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(requestC)),
            ).andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("Lecture time conflict"))

        val lectureD =
            dataGenerator.generateLectureandLocationTime(
                title = "Adjacent Lecture D",
                dayofWeek = 1,
                startTime = 1130,
                endTime = 1230,
            )
        val requestD = CreateTimetableLectureRequest(lectureId = lectureD.id!!)

        mvc
            .perform(
                post("/api/v1/timetables/${timetable.id!!}/lectures")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(requestD)),
            ).andExpect(status().isOk)
    }

    @Test
    fun `should not add a course to another user's timetable`() {
        // 다른 사람의 시간표에는 강의를 추가할 수 없다
        val (user1, token1) = dataGenerator.generateUser()
        val (user2, token2) = dataGenerator.generateUser()

        val timetable1 = dataGenerator.generateTimetable(user = user1)
        val lecture = dataGenerator.generateLectureandLocationTime()

        val request =
            CreateTimetableLectureRequest(
                lectureId = lecture.id!!,
            )

        mvc
            .perform(
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

        mvc
            .perform(
                delete("/api/v1/timetables/${timetable.id!!}/lectures/${lecture.id}")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON),
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

        mvc
            .perform(
                delete("/api/v1/timetables/${timetable1.id!!}/lectures/${lecture.id}")
                    .header("Authorization", "Bearer $token2")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `should fetch and save course information from SNU course registration site`() {
        // 서울대 수강신청 사이트에서 강의 정보를 가져와 저장할 수 있다

        val (user, token) = dataGenerator.generateUser()

        val year = 2025
        val semester = 2

        mvc
            .perform(
                post("/api/v1/lectures/fetch")
                    .header("Authorization", "Bearer $token")
                    .param("year", year.toString())
                    .param("semester", semester.toString()),
            ).andExpect(status().isOk)
        // .andExpect(jsonPath("$.message").value("Lecture sync successful for $year $semester"))

        val lectures = lectureRepository.findAll().toList()
        val locationTimes = locationTimeRepository.findAll().toList()

        println("Fetched and saved ${lectures.size} lectures.")
        println("Fetched and saved ${locationTimes.size} locationTimes.")

        assertTrue(lectures.size > 1000) { "강의 정보가 DB에 1000개 이상 저장되어야 합니다." }
        assertTrue(locationTimes.size > 1000) { "강의 시간/장소 정보가 DB에 1000개 이상 저장되어야 합니다." }
    }

    @Test
    fun `should return correct course list and total credits when retrieving timetable details`() {
        // 시간표 상세 조회 시, 강의 정보 목록과 총 학점이 올바르게 반환된다
        val (user, token) = dataGenerator.generateUser()
        val timetable = dataGenerator.generateTimetable(user = user, name = "User1 Timetable")

        val lecture1 = dataGenerator.generateLectureandLocationTime(credit = 2)
        val lecture2 = dataGenerator.generateLectureandLocationTime(credit = 3)
        val lecture3 = dataGenerator.generateLectureandLocationTime(credit = 4)
        val lecture4 = dataGenerator.generateLectureandLocationTime(credit = 5)

        dataGenerator.connectTimetableWithLecture(timetable, lecture1)
        dataGenerator.connectTimetableWithLecture(timetable, lecture2)
        dataGenerator.connectTimetableWithLecture(timetable, lecture3)
        dataGenerator.connectTimetableWithLecture(timetable, lecture4)

        queryCounter.assertQueryCount(5) {
            mvc
                .perform(
                    get("/api/v1/timetables/${timetable.id!!}")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.lectureAndLocationTimeDtos.length()").value(4))
                .andExpect(jsonPath("$.lectureAndLocationTimeDtos[0].lecture.title").value(lecture1.title))
                .andExpect(jsonPath("$.lectureAndLocationTimeDtos[0].lecture.credit").value(lecture1.credit))
                .andExpect(jsonPath("$.credits").value(14))
        }
    }

    @Test
    fun `should paginate correctly when searching for courses`() {
        // 강의 검색 시, 페이지네이션이 올바르게 동작한다
        val (user, token) = dataGenerator.generateUser()

        repeat(40) {
            dataGenerator.generateLectureWithSemester(2025, 1)
        }

        val response =
            mvc
                .perform(
                    get("/api/v1/lectures?year=2025&semester=1&size=20")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.paging.hasNext").value(true))
                .andReturn()
                .response
                .getContentAsString(Charsets.UTF_8)
                .let {
                    mapper.readValue(it, ListLectureResponse::class.java)
                }

        assertTrue(response.data.size == 20)

        val nextResponse =
            mvc
                .perform(
                    get("/api/v1/lectures?year=2025&semester=1&size=20&cursor=${response.paging.nextCursor}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.paging.hasNext").value(false))
                .andReturn()
                .response
                .getContentAsString(Charsets.UTF_8)
                .let {
                    mapper.readValue(it, ListLectureResponse::class.java)
                }

        assertTrue(nextResponse.data.size == 20)
    }
}
