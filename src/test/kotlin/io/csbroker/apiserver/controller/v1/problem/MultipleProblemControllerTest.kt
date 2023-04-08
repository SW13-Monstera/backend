package io.csbroker.apiserver.controller.v1.problem

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.ChoiceResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemAnswerDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemGradingHistoryDto
import io.csbroker.apiserver.service.problem.MultipleProblemService
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

class MultipleProblemControllerTest : RestDocsTest() {
    private lateinit var mockMvc: MockMvcRequestSpecification
    private lateinit var multipleProblemService: MultipleProblemService

    @BeforeEach
    fun setUp() {
        multipleProblemService = mockk()
        mockMvc = mockMvc(
            MultipleProblemController(
                multipleProblemService,
            ),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `Multiple Choice problem 단건 조회`() {
        // given
        every { multipleProblemService.findProblemById(any(), any()) } returns MultipleChoiceProblemDetailResponseDto(
            id = 1L,
            title = "problem title",
            tags = listOf("tag1", "tag2"),
            description = "problem description",
            correctSubmission = 10,
            correctUserCnt = 10,
            score = 10.0,
            totalSubmission = 10,
            isSolved = true,
            isMultipleAnswer = true,
            choices = listOf(ChoiceResponseDto(1, "123")),
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/v1/problems/multiple/{problem_id}", 1L)

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "problems/multiple/inquire",
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
                        fieldWithPath("data.correctUserCnt").type(JsonFieldType.NUMBER)
                            .description("맞은 사람 수"),
                        fieldWithPath("data.correctSubmission").type(JsonFieldType.NUMBER)
                            .description("맞은 제출 수"),
                        fieldWithPath("data.totalSubmission").type(JsonFieldType.NUMBER)
                            .description("총 제출 수"),
                        fieldWithPath("data.choices.[].id").type(JsonFieldType.NUMBER)
                            .description("선지 id"),
                        fieldWithPath("data.choices.[].content").type(JsonFieldType.STRING)
                            .description("선지 내용"),
                        fieldWithPath("data.isSolved").type(JsonFieldType.BOOLEAN)
                            .description("푼 문제 여부"),
                        fieldWithPath("data.isMultipleAnswer").type(JsonFieldType.BOOLEAN)
                            .description("다중 답안 여부"),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("문제 배점"),
                    ),
                ),
            )
    }

    @Test
    fun `Multiple choice problem 채점`() {
        // given
        every { multipleProblemService.gradingProblem(any()) } returns MultipleChoiceProblemGradingHistoryDto(
            gradingHistoryId = 1L,
            problemId = 1L,
            title = "problem title",
            tags = listOf("tag1", "tag2"),
            description = "problem description",
            correctSubmission = 10,
            correctUserCnt = 10,
            score = 10.0,
            totalSubmission = 10,
            choices = listOf(ChoiceResponseDto(1, "123")),
            userAnswerIds = listOf(1L),
            isAnswer = true,
        )

        // when
        val result = mockMvc.body(
            MultipleChoiceProblemAnswerDto(
                answerIds = listOf(1L),
            ),
        ).request(
            Method.POST,
            "/api/v1/problems/multiple/{problem_id}/grade",
            1L,
        )

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "problems/multiple/grade",
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
                        fieldWithPath("data.userAnswerIds").type(JsonFieldType.ARRAY)
                            .description("채점 된 유저 답안 ( 고른 선지의 id 배열 )"),
                        fieldWithPath("data.isAnswer").type(JsonFieldType.BOOLEAN)
                            .description("유저 답안의 정답 여부"),
                        fieldWithPath("data.choices").type(JsonFieldType.ARRAY)
                            .description("문제의 선지"),
                        fieldWithPath("data.choices").type(JsonFieldType.ARRAY)
                            .description("문제의 선지"),
                        fieldWithPath("data.choices").type(JsonFieldType.ARRAY)
                            .description("문제의 선지"),
                        fieldWithPath("data.choices.[].id").type(JsonFieldType.NUMBER)
                            .description("선지 id"),
                        fieldWithPath("data.choices.[].content").type(JsonFieldType.STRING)
                            .description("선지 내용"),
                    ),
                ),
            )
    }
}
