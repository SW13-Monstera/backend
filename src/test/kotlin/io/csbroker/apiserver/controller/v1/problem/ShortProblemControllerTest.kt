package io.csbroker.apiserver.controller.v1.problem

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemAnswerDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemGradingHistoryDto
import io.csbroker.apiserver.service.problem.ShortProblemService
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

class ShortProblemControllerTest : RestDocsTest() {
    private lateinit var mockMvc: MockMvcRequestSpecification
    private lateinit var shortProblemService: ShortProblemService

    @BeforeEach
    fun setUp() {
        shortProblemService = mockk()
        mockMvc = mockMvc(
            ShortProblemController(
                shortProblemService,
            ),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `Short problem 단건 조회`() {
        // given
        every { shortProblemService.findProblemById(any(), any()) } returns ShortProblemDetailResponseDto(
            id = 1L,
            title = "problem title",
            tags = listOf("tag1", "tag2"),
            description = "problem description",
            correctSubmission = 10,
            correctUserCnt = 10,
            score = 10.0,
            totalSubmission = 10,
            answerLength = 10,
            isEnglish = true,
            isSolved = true,
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/v1/problems/short/{problem_id}", 1L)

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "problems/short/inquire",
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
                        fieldWithPath("data.correctUserCnt").type(JsonFieldType.NUMBER)
                            .description("맞은 사람 수"),
                        fieldWithPath("data.correctSubmission").type(JsonFieldType.NUMBER)
                            .description("맞은 제출 수"),
                        fieldWithPath("data.totalSubmission").type(JsonFieldType.NUMBER)
                            .description("총 제출 수"),
                        fieldWithPath("data.answerLength").type(JsonFieldType.NUMBER)
                            .description("정답 글자수 ( 힌트 )"),
                        fieldWithPath("data.isEnglish").type(JsonFieldType.BOOLEAN)
                            .description("정답 언어 ( 영어면 true, 한국어면 false )"),
                        fieldWithPath("data.isSolved").type(JsonFieldType.BOOLEAN)
                            .description("푼 문제 여부"),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("문제 배점"),
                    ),
                ),
            )
    }

    @Test
    fun `Short problem 채점`() {
        // given
        every { shortProblemService.gradingProblem(any()) } returns ShortProblemGradingHistoryDto(
            gradingHistoryId = 1L,
            problemId = 1L,
            title = "problem title",
            tags = listOf("tag1", "tag2"),
            description = "problem description",
            correctSubmission = 10,
            correctUserCnt = 10,
            score = 10.0,
            userAnswer = "user answer",
            totalSubmission = 10,
            isAnswer = false,
            answerLength = 10,
            correctAnswer = "correct answer",
        )

        // when
        val result = mockMvc.body(
            ShortProblemAnswerDto(
                answer = "user answer",
            ),
        ).request(Method.POST, "/api/v1/problems/short/{problem_id}/grade", 1L)

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "problems/short/grade",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰")
                            .optional(),
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
                        fieldWithPath("data.correctUserCnt").type(JsonFieldType.NUMBER)
                            .description("맞은 사람 수"),
                        fieldWithPath("data.correctSubmission").type(JsonFieldType.NUMBER)
                            .description("맞은 제출 수"),
                        fieldWithPath("data.totalSubmission").type(JsonFieldType.NUMBER)
                            .description("총 제출 수"),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("채점 된 유저 답안의 점수"),
                        fieldWithPath("data.userAnswer").type(JsonFieldType.STRING)
                            .description("채점 된 유저 답안"),
                        fieldWithPath("data.answerLength").type(JsonFieldType.NUMBER)
                            .description("모범 답안의 글자 수"),
                        fieldWithPath("data.isAnswer").type(JsonFieldType.BOOLEAN)
                            .description("유저 답안의 정답 여부"),
                        fieldWithPath("data.correctAnswer").type(JsonFieldType.STRING)
                            .description("모범 답안"),
                    ),
                ),
            )
    }
}
