package io.csbroker.apiserver.controller.v1.problem

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.controller.v2.problem.response.SubmitLongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemAnswerDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import io.csbroker.apiserver.service.post.PostService
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
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters

class LongProblemControllerTest : RestDocsTest() {
    private lateinit var mockMvc: MockMvcRequestSpecification
    private lateinit var longProblemService: LongProblemService
    private lateinit var postService: PostService

    @BeforeEach
    fun setUp() {
        longProblemService = mockk()
        postService = mockk()
        mockMvc = mockMvc(
            LongProblemController(
                longProblemService,
                postService,
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
            bookmarkCount = 10,
            likeCount = 10,
            isBookmarked = true,
            isLiked = true,
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/v1/problems/long/{problem_id}", "1")

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
                        fieldWithPath("data.bookmarkCount").type(JsonFieldType.NUMBER)
                            .description("북마크 수"),
                        fieldWithPath("data.likeCount").type(JsonFieldType.NUMBER)
                            .description("좋아요 수"),
                        fieldWithPath("data.isBookmarked").type(JsonFieldType.BOOLEAN)
                            .description("북마크 여부"),
                        fieldWithPath("data.isLiked").type(JsonFieldType.BOOLEAN)
                            .description("좋아요 여부"),
                    ),
                ),
            )
    }

    @Test
    fun `서술형 문제 제출`() {
        // given
        val responseDto = SubmitLongProblemResponseDto(
            title = "title",
            tags = listOf("tag1", "tag2", "tag3"),
            description = "description",
            totalSubmissionCount = 100,
            userSubmissionCount = 10,
            userAnswer = "user answer",
            standardAnswer = "standard answer",
        )
        every { longProblemService.submitProblem(any()) } returns responseDto
        every { postService.create(any(), any(), any()) } returns 1L

        // when

        val result = mockMvc.body(
            LongProblemAnswerDto(
                answer = "user answer",
            ),
        ).request(Method.POST, "/api/v1/problems/long/{problem_id}/submit", "1")

        result.then()
            .statusCode(200)
            .apply(
                document(
                    "problems/long/submit",
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
                    requestFields(
                        fieldWithPath("answer").type(JsonFieldType.STRING).description("유저가 작성한 답안"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.title").type(JsonFieldType.STRING).description("문제 제목"),
                        fieldWithPath("data.tags").type(JsonFieldType.ARRAY).description("태그"),
                        fieldWithPath("data.description").type(JsonFieldType.STRING).description("문제 설명"),
                        fieldWithPath("data.totalSubmissionCount").type(JsonFieldType.NUMBER)
                            .description("해당 문제에 대해 전체 유저가 제출한 수 (총 제출 수)"),
                        fieldWithPath("data.userSubmissionCount").type(JsonFieldType.NUMBER)
                            .description("해당 문제에 대해 유저가 제출한 수 "),
                        fieldWithPath("data.userAnswer").type(JsonFieldType.STRING).description("유저의 답변"),
                        fieldWithPath("data.standardAnswer").type(JsonFieldType.STRING).description("모범 답안"),
                    ),
                ),
            )
    }
}
