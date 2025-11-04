package com.wafflestudio.spring2025.lecture.repository

import com.wafflestudio.spring2025.lecture.util.SugangSnuApi
import com.wafflestudio.spring2025.lecture.util.SugangSnuUrlUtils.convertSemesterToSugangSnuSearchString
import org.springframework.core.io.buffer.PooledDataBuffer
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class SugangSnuRepository(
    private val sugangSnuApi: SugangSnuApi,
) {
    companion object {
        const val SUGANG_SNU_LECTURE_EXCEL_DOWNLOAD_PATH = "/sugang/cc/cc100InterfaceExcel.action"
        val DEFAULT_LECTURE_EXCEL_DOWNLOAD_PARAMS =
            """
            seeMore=더보기&
            srchBdNo=&srchCamp=&srchOpenSbjtFldCd=&srchCptnCorsFg=&
            srchCurrPage=1&
            srchExcept=&srchGenrlRemoteLtYn=&srchIsEngSbjt=&
            srchIsPendingCourse=&srchLsnProgType=&srchMrksApprMthdChgPosbYn=&srchMrksGvMthd=&
            srchOpenUpDeptCd=&srchOpenMjCd=&srchOpenPntMax=&srchOpenPntMin=&srchOpenSbjtDayNm=&
            srchOpenSbjtNm=&srchOpenSbjtTm=&srchOpenSbjtTmNm=&srchOpenShyr=&srchOpenSubmattCorsFg=&
            srchOpenSubmattFgCd1=&srchOpenSubmattFgCd2=&srchOpenSubmattFgCd3=&srchOpenSubmattFgCd4=&
            srchOpenSubmattFgCd5=&srchOpenSubmattFgCd6=&srchOpenSubmattFgCd7=&srchOpenSubmattFgCd8=&
            srchOpenSubmattFgCd9=&srchOpenDeptCd=&srchOpenUpSbjtFldCd=&
            srchPageSize=9999&
            srchProfNm=&srchSbjtCd=&srchSbjtNm=&srchTlsnAplyCapaCntMax=&srchTlsnAplyCapaCntMin=&srchTlsnRcntMax=&srchTlsnRcntMin=&
            workType=EX
            """.trimIndent().replace("\n", "")
    }

    /**
     * 강의 Excel 파일을 다운로드합니다.
     */
    fun getSugangSnuLectures(
        year: Int,
        semester: Int,
        language: String = "ko",
    ): PooledDataBuffer? {
        return sugangSnuApi
            .get()
            .uri { builder ->
                builder.run {
                    path(SUGANG_SNU_LECTURE_EXCEL_DOWNLOAD_PATH)
                    query(DEFAULT_LECTURE_EXCEL_DOWNLOAD_PARAMS)
                    queryParam("srchLanguage", language)
                    queryParam("srchOpenSchyy", year)
                    queryParam("srchOpenShtm", convertSemesterToSugangSnuSearchString(semester))
                    build()
                }
            }.accept(MediaType.TEXT_HTML) // Excel이지만 text/html로 응답이 옵니다.
            .retrieve()
            .bodyToMono(PooledDataBuffer::class.java)
            .block()
            }
}
