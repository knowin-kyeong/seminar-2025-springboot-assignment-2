package com.wafflestudio.spring2025.lecture.service

import com.wafflestudio.spring2025.lecture.model.Lecture
import com.wafflestudio.spring2025.lecture.repository.LectureRepository
import com.wafflestudio.spring2025.lecture.repository.SugangSnuRepository
import com.wafflestudio.spring2025.locationtime.model.LocationTime
import com.wafflestudio.spring2025.locationtime.repository.LocationTimeRepository
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * SNU 수강 사이트의 강의 정보를 동기화하는 서비스입니다.
 *
 * 1. [SugangSnuRepository]를 통해 Excel 파일을 다운로드합니다.
 * 2. Excel 파일을 파싱하여 [Lecture] 및 [LocationTime] 엔티티로 변환합니다.
 * 3. [LectureRepository] 및 [LocationTimeRepository]를 통해 데이터베이스에 저장합니다.
 */
@Service
class SugangSnuLectureSyncService(
    private val sugangSnuRepository: SugangSnuRepository,
    private val lectureRepository: LectureRepository,
    private val locationTimeRepository: LocationTimeRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // "월(09:00~11:50)" 형식의 수업시간을 파싱하기 위한 정규식
    private val classTimeRegex = """([월화수목금토일])\((\d{2}:\d{2})~(\d{2}:\d{2})\)""".toRegex()

    /**
     * 특정 학기의 강의 정보를 SNU 수강 사이트에서 가져와 DB에 동기화합니다.
     * 'ko' (한국어) Excel 파일만 파싱하여 저장합니다.
     * @Transactional 어노테이션을 위해 'open' 키워드를 사용합니다.
     */
    @Transactional
    open fun syncLectures(
        year: Int,
        semester: Int,
    ) {
        // 1. SugangSnuRepository를 통해 Excel 파일 데이터(Buffer)를 가져옵니다.
        val lectureXlsx = sugangSnuRepository.getSugangSnuLectures(year, semester, "ko")!!
        log.info("Fetching lectures for $year-$semester...")
        log.debug("Downloaded Excel file size: ${lectureXlsx.readableByteCount()} bytes")

        try {
            // 2. Apache POI를 사용하여 Excel 파일을 파싱합니다.
            val workbook = HSSFWorkbook(lectureXlsx.asInputStream())
            val sheet = workbook.getSheetAt(0)

            // 엑셀 헤더(컬럼명) 행 (Row index 2)
            val headerRow =
                sheet.getRow(2) ?: run {
                    log.error("Header row not found in Excel file.")
                    return
                }
            // 컬럼 이름을 인덱스로 매핑합니다. (예: "교과목번호" -> 3)
            val columnMap =
                headerRow.cellIterator().asSequence().associate {
                    it.stringCellValue.trim() to it.columnIndex
                }

            // 실제 데이터 행 (Row index 3부터)
            val dataRows = (3..sheet.lastRowNum).mapNotNull { sheet.getRow(it) }

            log.info("Found ${dataRows.size} rows in Excel file. Start parsing and saving...")

            for (row in dataRows) {
                // 행의 첫 번째 셀이 비어있으면 데이터 끝으로 간주
                if (row.getCell(0)?.stringCellValue.isNullOrEmpty()) break

                // 3. 행(Row)을 Lecture와 LocationTime DTO로 파싱
                val (lecture, locationTimeDtos) = parseRowToEntities(row, columnMap, year, semester)

                // 4. Lecture 저장
                val savedLecture = lectureRepository.save(lecture)
                val lectureId = savedLecture.id ?: continue // id가 null이면 저장 실패

                // 5. LocationTime 엔티티 생성 및 저장
                val locationTimeEntities =
                    locationTimeDtos.map { dto ->
                        LocationTime(
                            lectureId = lectureId,
                            dayOfWeek = dto.dayOfWeek,
                            startTime = dto.startTime,
                            endTime = dto.endTime,
                            location = dto.location,
                        )
                    }
                locationTimeRepository.saveAll(locationTimeEntities).toList() // Flow를 소비하여 저장 실행
            }

            log.info("Successfully synced ${dataRows.size} lectures for $year-$semester.")
        } catch (e: Exception) {
            log.error("Failed to sync lectures: ${e.message}", e)
            // @Transactional에 의해 예외 발생 시 롤백됩니다.
            throw e // 예외를 다시 던져서 컨트롤러가 알 수 있도록 함
        } finally {
            lectureXlsx.release() // PooledDataBuffer 릴리스 (매우 중요)
        }
    }

    /**
     * Excel의 한 행(Row)을 Lecture 엔티티와 LocationTime DTO 리스트로 변환합니다.
     */
    private fun parseRowToEntities(
        row: Row,
        columnMap: Map<String, Int>,
        year: Int,
        semester: Int,
    ): Pair<Lecture, List<ParsedLocationTime>> {
        // 셀 값을 안전하게 가져오는 헬퍼 함수
        fun getCell(name: String): String {
            val index = columnMap[name]
            return if (index != null) {
                row.getCell(index)?.stringCellValue?.trim() ?: ""
            } else {
                log.warn("Column '$name' not found in Excel header.")
                ""
            }
        }

        // 1. Lecture 엔티티 파싱
        val classification = getCell("교과구분")
        val college = getCell("개설대학")
        val department = getCell("개설학과")
        val academicCourse = getCell("이수과정")
        val academicYear = getCell("학년")

        val lecture =
            Lecture(
                year = year,
                semester = semester,
                lectureNumber = getCell("교과목번호"),
                classNumber = getCell("강좌번호"),
                title = getCell("교과목명"),
                subtitle = getCell("부제명").ifEmpty { null },
                credit = getCell("학점").toIntOrNull() ?: 0,
                classification = classification,
                college = college,
                // 원본 SNUTT 코드 로직 반영: 'null' 문자열 제거 및 학과가 비어있으면 대학으로 대체
                department = department.replace("null", "").ifEmpty { college },
                academicCourse = academicCourse,
                // 원본 SNUTT 코드 로직 반영: 이수과정이 '학사'가 아니면 이수과정을, 아니면 학년을 사용
                academicYear = academicCourse.takeIf { it != "학사" } ?: academicYear,
                instructor = getCell("주담당교수"),
            )

        // 2. LocationTime DTO 파싱
        val classTimeText = getCell("수업교시")
        val locationText = getCell("강의실(동-호)(#연건, *평창)")
        val locationTimes = parseClassTimes(classTimeText, locationText)

        return lecture to locationTimes
    }

    /**
     * "수업교시"와 "강의실" 텍스트를 파싱하여 LocationTime DTO 리스트로 반환합니다.
     *
     * @param classTimeText 예: "월(10:00~11:50)/수(10:00~11:50)"
     * @param locationText 예: "301-101/301-101"
     */
    private fun parseClassTimes(
        classTimeText: String,
        locationText: String,
    ): List<ParsedLocationTime> {
        if (classTimeText.isBlank()) {
            return emptyList()
        }

        val timeTokens = classTimeText.split("/")
        val locationTokens = locationText.split("/")

        return timeTokens.mapIndexedNotNull { index, timeToken ->
            val match = classTimeRegex.find(timeToken) ?: return@mapIndexedNotNull null

            val dayChar = match.groupValues[1]
            val startTimeStr = match.groupValues[2]
            val endTimeStr = match.groupValues[3]

            // 요일: 월=0, 화=1, ..., 일=6
            val dayOfWeek =
                when (dayChar) {
                    "월" -> 0
                    "화" -> 1
                    "수" -> 2
                    "목" -> 3
                    "금" -> 4
                    "토" -> 5
                    "일" -> 6
                    else -> return@mapIndexedNotNull null
                }

            // 시간: 09:30 -> 9 * 60 + 30 = 570 (자정부터의 분)
            val startTime = parseTimeToMinutes(startTimeStr)
            val endTime = parseTimeToMinutes(endTimeStr)

            // 인덱스에 맞는 강의실 정보 매칭
            val location = locationTokens.getOrNull(index)?.ifEmpty { null }

            ParsedLocationTime(dayOfWeek, startTime, endTime, location)
        }
    }

    /**
     * "HH:mm" 형식의 시간을 분(Int)으로 변환합니다.
     */
    private fun parseTimeToMinutes(time: String): Int {
        try {
            val (hour, minute) = time.split(":").map { it.toInt() }
            return hour * 60 + minute
        } catch (e: Exception) {
            log.warn("Failed to parse time string: $time")
            return 0
        }
    }

    /**
     * 파싱된 LocationTime을 임시로 담기 위한 내부 DTO
     */
    private data class ParsedLocationTime(
        val dayOfWeek: Int,
        val startTime: Int, // 분(minute)
        val endTime: Int, // 분(minute)
        val location: String?,
    )
}
