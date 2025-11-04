package com.wafflestudio.spring2025.helper

import com.wafflestudio.spring2025.board.model.Board
import com.wafflestudio.spring2025.board.repository.BoardRepository
import com.wafflestudio.spring2025.comment.model.Comment
import com.wafflestudio.spring2025.comment.repository.CommentRepository
import com.wafflestudio.spring2025.lecture.model.Lecture
import com.wafflestudio.spring2025.lecture.repository.LectureRepository
import com.wafflestudio.spring2025.locationtime.model.LocationTime
import com.wafflestudio.spring2025.locationtime.repository.LocationTimeRepository
import com.wafflestudio.spring2025.post.model.Post
import com.wafflestudio.spring2025.post.repository.PostRepository
import com.wafflestudio.spring2025.timetable.model.Timetable
import com.wafflestudio.spring2025.timetable.repository.TimetableRepository
import com.wafflestudio.spring2025.timetableLecture.model.TimetableLecture
import com.wafflestudio.spring2025.timetableLecture.repository.TimetableLectureRepository
import com.wafflestudio.spring2025.user.JwtTokenProvider
import com.wafflestudio.spring2025.user.model.User
import com.wafflestudio.spring2025.user.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component
import kotlin.String
import kotlin.random.Random

@Component
class DataGenerator(
    private val userRepository: UserRepository,
    private val boardRepository: BoardRepository,
    private val postRepository: PostRepository,
    private val timetableRepository: TimetableRepository,
    private val commentRepository: CommentRepository,
    private val lectureRepository: LectureRepository,
    private val locationTimeRepository: LocationTimeRepository,
    private val timetableLectureRepository: TimetableLectureRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    fun generateUser(
        username: String? = null,
        password: String? = null,
    ): Pair<User, String> {
        val user =
            userRepository.save(
                User(
                    username = username ?: "user-${Random.Default.nextInt(1000000)}",
                    password = BCrypt.hashpw(password ?: "password-${Random.Default.nextInt(1000000)}", BCrypt.gensalt()),
                ),
            )
        return user to jwtTokenProvider.createToken(user.username)
    }

    fun generateBoard(name: String? = null): Board {
        val board =
            boardRepository.save(
                Board(
                    name = name ?: "board-${Random.Default.nextInt(1000000)}",
                ),
            )
        return board
    }

    fun generatePost(
        title: String? = null,
        content: String? = null,
        user: User? = null,
        board: Board? = null,
    ): Post {
        val post =
            postRepository.save(
                Post(
                    title = title ?: "title-${Random.Default.nextInt(1000000)}",
                    content = content ?: "content-${Random.Default.nextInt(1000000)}",
                    userId = (user ?: generateUser().first).id!!,
                    boardId = (board ?: generateBoard()).id!!,
                ),
            )
        return post
    }

    fun generateComment(
        content: String? = null,
        user: User? = null,
        post: Post? = null,
    ): Comment {
        val comment =
            commentRepository.save(
                Comment(
                    content = content ?: "content-${Random.Default.nextInt(1000000)}",
                    userId = (user ?: generateUser().first).id!!,
                    postId = (post ?: generatePost()).id!!,
                ),
            )
        return comment
    }

    fun generateTimetable(
        user: User,
        name: String = "Default Name",
        year: Int = 2025,
        semester: Int = 1,
    ): Timetable =
        timetableRepository.save(
            Timetable(
                userId = user.id!!,
                name = name,
                year = year,
                semester = semester,
            ),
        )

    fun generateLectureandLocationTime(
        title: String? = null,
        credit: Int = 3,
        dayofWeek: Int = 0,
        startTime: Int = 0,
        endTime: Int = 90,
    ): Lecture {
        val lecture =
            lectureRepository.save(
                Lecture(
                    year = 2025,
                    semester = 2,
                    title = title ?: "title-${Random.nextInt(10000)}",
                    subtitle = null,
                    lectureNumber = Random.nextInt(10000).toString(),
                    classNumber = Random.nextInt(1000).toString(),
                    credit = credit,
                    classification = "전선",
                    college = "공과대학",
                    department = "컴퓨터공학부",
                    academicCourse = "학사",
                    academicYear = "3",
                    instructor = "문봉기",
                ),
            )

        val locationTime =
            locationTimeRepository.save(
                LocationTime(
                    lectureId = lecture.id!!,
                    dayOfWeek = dayofWeek,
                    startTime = startTime,
                    endTime = endTime,
                ),
            )

        return lecture
    }

    fun generateLectureWithSemester(
        year: Int,
        semester: Int,
        title: String? = null,
    ): Lecture {
        val lecture =
            lectureRepository.save(
                Lecture(
                    year = year,
                    semester = semester,
                    title = title ?: "title-${Random.nextInt(10000)}",
                    subtitle = null,
                    lectureNumber = Random.nextInt(10000).toString(),
                    classNumber = Random.nextInt(1000).toString(),
                    credit = 3,
                    classification = "전선",
                    college = "공과대학",
                    department = "컴퓨터공학부",
                    academicCourse = "학사",
                    academicYear = "3",
                    instructor = "문봉기",
                ),
            )

        return lecture
    }

    fun connectTimetableWithLecture(
        timetable: Timetable,
        lecture: Lecture,
    ): TimetableLecture {
        val timetableLecture =
            timetableLectureRepository.save(
                TimetableLecture(
                    timetableId = timetable.id!!,
                    lectureId = lecture.id!!,
                ),
            )

        return timetableLecture
    }
}
