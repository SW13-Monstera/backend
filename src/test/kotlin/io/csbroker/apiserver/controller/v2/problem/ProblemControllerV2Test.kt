package io.csbroker.apiserver.controller.v2.problem

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.controller.v2.problem.request.ChallengeCreateRequest
import io.csbroker.apiserver.service.problem.CommonProblemService
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

class ProblemControllerV2Test : RestDocsTest() {
    private lateinit var mockMvc: MockMvcRequestSpecification
    private lateinit var commonProblemService: CommonProblemService

    @BeforeEach
    fun setup() {
        commonProblemService = mockk()
        mockMvc = mockMvc(
            ProblemControllerV2(commonProblemService),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `문제 이의 제기`() {
        // given
        every { commonProblemService.createChallenge(any()) } returns Unit

        // when
        val result = mockMvc.body(ChallengeCreateRequest("이것은 이의제기를 위한 내용입니다."))
            .request(Method.POST, "/api/v2/problems/{problem_id}/challenge", 1L)

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "problems/v2/challenge",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("problem_id").description("문제 id"),
                    ),
                    requestFields(
                        fieldWithPath("content").type(JsonFieldType.STRING)
                            .description("이의제기 내용 ( 최소 10자 ~ 최대 150자 )"),
                    ),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰")
                            .optional(),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data").type(JsonFieldType.BOOLEAN).description("이의제기 데이터 생성 여부"),
                    ),
                ),
            )
    }
}
