package io.csbroker.apiserver.controller.v2.problem

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.controller.v2.problem.response.ShortProblemAnswerType
import io.csbroker.apiserver.controller.v2.problem.response.ShortProblemDetailResponseV2Dto
import io.csbroker.apiserver.service.problem.ShortProblemService
import io.mockk.every
import io.mockk.mockk
import io.restassured.http.Method
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters

class ShortProblemControllerV2Test : RestDocsTest() {
    private lateinit var mockMvc: MockMvcRequestSpecification
    private lateinit var shortProblemService: ShortProblemService

    @BeforeEach
    fun setup() {
        shortProblemService = mockk()
        mockMvc = mockMvc(
            ShortProblemControllerV2(shortProblemService),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `Short problem 단건 조회`() {
        // given
        every {
            shortProblemService.findShortProblemDetailByIdV2(
                any(),
                any(),
            )
        } returns ShortProblemDetailResponseV2Dto(
            id = 1L,
            title = "problem title",
            tags = listOf("tag1", "tag2"),
            description = "problem description",
            correctSubmission = 10,
            correctUserCnt = 10,
            score = 10.0,
            totalSubmission = 10,
            answerLength = 10,
            consistOf = ShortProblemAnswerType.ENGLISH,
            isSolved = true,
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/v2/problems/short/{problem_id}", 1L)

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "problems/v2/short/inquire",
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
                        fieldWithPath("data.consistOf").type(JsonFieldType.STRING)
                            .description("정답 언어 ( 영어면 ENGLISH, 한국어면 KOREAN, 숫자면 NUMERIC )"),
                        fieldWithPath("data.isSolved").type(JsonFieldType.BOOLEAN)
                            .description("푼 문제 여부"),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("문제 배점"),
                    ),
                ),
            )
    }
}
