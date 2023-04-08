package io.csbroker.apiserver.controller.v1.problem

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.problem.longproblem.ContentDto
import io.csbroker.apiserver.dto.problem.longproblem.KeywordDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemAnswerDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemGradingHistoryDto
import io.csbroker.apiserver.service.problem.LongProblemService
import io.mockk.every
import io.mockk.mockk
import io.restassured.http.Method
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters

class LongProblemControllerTest : RestDocsTest() {
    private lateinit var mockMvc: MockMvcRequestSpecification
    private lateinit var longProblemService: LongProblemService

    @BeforeEach
    fun setUp() {
        longProblemService = mockk()
        mockMvc = mockMvc(
            LongProblemController(
                longProblemService,
            ),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `Long problem 단건 조회`() {
        // given
        every { longProblemService.findProblemById(any(), any()) } returns LongProblemDetailResponseDto(
            id = 1L,
            title = "Long problem title",
            tags = listOf("tag1", "tag2"),
            description = "Long problem description",
            avgScore = 10.0,
            topScore = 10.0,
            bottomScore = 0.0,
            score = 10.0,
            totalSubmission = 10,
            isSolved = true,
            isGradable = true,
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/v1/problems/long/{problem_id}", 1L)

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "problems/long/inquire",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("problem_id").description("문제 id"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("문제 id"),
                        fieldWithPath("data.title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        fieldWithPath("data.tags").type(JsonFieldType.ARRAY).description("태그"),
                        fieldWithPath("data.avgScore").type(JsonFieldType.NUMBER)
                            .description("평균 점수 ( 푼 사람이 없는 경우 null return )").optional(),
                        fieldWithPath("data.topScore").type(JsonFieldType.NUMBER)
                            .description("최고 점수 ( 푼 사람이 없는 경우 null return )").optional(),
                        fieldWithPath("data.bottomScore").type(JsonFieldType.NUMBER)
                            .description("최저 점수 ( 푼 사람이 없는 경우 null return )").optional(),
                        fieldWithPath("data.totalSubmission").type(JsonFieldType.NUMBER)
                            .description("총 제출 수"),
                        fieldWithPath("data.isSolved").type(JsonFieldType.BOOLEAN)
                            .description("푼 문제 여부"),
                        fieldWithPath("data.isGradable").type(JsonFieldType.BOOLEAN)
                            .description("문제 채점 가능 여부"),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("문제 배점"),
                    ),
                ),
            )
    }

    @Test
    fun `Long problem 채점`() {
        // given
        every { longProblemService.gradingProblem(any()) } returns LongProblemGradingHistoryDto(
            gradingHistoryId = 1L,
            problemId = 1L,
            title = "Long problem title",
            tags = listOf("tag1", "tag2"),
            description = "Long problem description",
            avgScore = 10.0,
            topScore = 10.0,
            bottomScore = 0.0,
            score = 10.0,
            totalSubmission = 10,
            keywords = listOf(KeywordDto(1, "keyword1")),
            contents = listOf(ContentDto(1, "content1")),
            userAnswer = "정답의 키워드는 test를 포함해야합니다.",
            standardAnswer = "정답의 키워드는 test를 포함해야합니다.",
        )

        // when
        val result = mockMvc.body(
            LongProblemAnswerDto(
                answer = "정답의 키워드는 test를 포함해야합니다.",
            ),
        ).request(Method.POST, "/api/v1/problems/long/{problem_id}/grade?isGrading=true", 1L)

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "problems/long/grade",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰")
                            .optional(),
                    ),
                    requestParameters(
                        parameterWithName("isGrading").description("실제 채점 진행 여부 ( default = true )"),
                    ),
                    pathParameters(
                        parameterWithName("problem_id").description("문제 id"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.gradingHistoryId").type(JsonFieldType.NUMBER).description("채점 결과 id"),
                        fieldWithPath("data.problemId").type(JsonFieldType.NUMBER).description("문제 id"),
                        fieldWithPath("data.title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        fieldWithPath("data.tags").type(JsonFieldType.ARRAY).description("태그"),
                        fieldWithPath("data.avgScore").type(JsonFieldType.NUMBER)
                            .description("평균 점수 ( 푼 사람이 없는 경우 null return )").optional(),
                        fieldWithPath("data.topScore").type(JsonFieldType.NUMBER)
                            .description("최고 점수 ( 푼 사람이 없는 경우 null return )").optional(),
                        fieldWithPath("data.bottomScore").type(JsonFieldType.NUMBER)
                            .description("최저 점수 ( 푼 사람이 없는 경우 null return )").optional(),
                        fieldWithPath("data.totalSubmission").type(JsonFieldType.NUMBER)
                            .description("총 제출 수"),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("채점 된 유저 답안의 점수"),
                        fieldWithPath("data.userAnswer").type(JsonFieldType.STRING)
                            .description("채점 된 유저 답안"),
                        fieldWithPath("data.standardAnswer").type(JsonFieldType.STRING)
                            .description("문제의 모범 답안"),
                        fieldWithPath("data.keywords").type(JsonFieldType.ARRAY)
                            .description("답안에 들어가야하는 키워드"),
                        fieldWithPath("data.keywords.[].id").type(JsonFieldType.NUMBER)
                            .description("키워드 id"),
                        fieldWithPath("data.keywords.[].content").type(JsonFieldType.STRING)
                            .description("키워드 내용"),
                        fieldWithPath("data.keywords.[].isExist").type(JsonFieldType.BOOLEAN)
                            .description("키워드가 유저답안에 존재하는지 유무"),
                        fieldWithPath("data.keywords.[].idx").type(JsonFieldType.ARRAY)
                            .description("키워드가 유저답안에 존재 할 때, 시작 index와 끝 index ( 존재하지 않으면 빈 배열 )"),
                        fieldWithPath("data.contents").type(JsonFieldType.ARRAY)
                            .description("답안에 들어가야하는 내용 채점 기준"),
                        fieldWithPath("data.contents.[].id").type(JsonFieldType.NUMBER)
                            .description("내용 채점 기준 id"),
                        fieldWithPath("data.contents.[].content").type(JsonFieldType.STRING)
                            .description("내용 채점 기준 내용"),
                        fieldWithPath("data.contents.[].isExist").type(JsonFieldType.BOOLEAN)
                            .description("내용 채점 기준이 유저답안에 존재하는지 유무"),
                    ),
                ),
            )
    }
}
