package io.csbroker.apiserver.controller.v1.problem

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.problem.ProblemResponseDto
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetDetailResponseDto
import io.csbroker.apiserver.model.ProblemSet
import io.csbroker.apiserver.service.problem.ProblemSetService
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

class ProblemSetControllerTest : RestDocsTest() {
    private lateinit var mockMvc: MockMvcRequestSpecification
    private lateinit var problemSetService: ProblemSetService

    @BeforeEach
    fun setUp() {
        problemSetService = mockk()
        mockMvc = mockMvc(ProblemSetController(problemSetService))
    }

    @Test
    fun `문제 세트 전체 조회`() {
        // given
        every { problemSetService.findAll() } returns listOf(ProblemSet(1, "test 문제세트", "테스트용 문제세트"))

        // when
        val response = mockMvc.request(Method.GET, "/api/v1/problem-sets")

        // then
        response.then().statusCode(200)
            .apply(
                document(
                    "problem-sets-findall",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.[].id").type(JsonFieldType.NUMBER).description("문제 세트 ID"),
                        fieldWithPath("data.[].problemCnt").type(JsonFieldType.NUMBER).description("문제 세트에 포함된 문제 수"),
                        fieldWithPath("data.[].name").type(JsonFieldType.STRING).description("문제 세트 이름"),
                        fieldWithPath("data.[].description").type(JsonFieldType.STRING).description("문제 세트 설명"),
                    ),
                ),
            )
    }

    @Test
    fun `문제 세트 단건 조회`() {
        // given
        every {
            problemSetService.findById(any()).toProblemSetDetailResponseDto()
        } returns createProblemsetDetailResponseDto()

        // when
        val response = mockMvc.request(Method.GET, "/api/v1/problem-sets/1")

        // then
        response.then().statusCode(200)
            .apply(
                document(
                    "problem-sets-find",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("문제 세트 ID"),
                        fieldWithPath("data.name").type(JsonFieldType.STRING).description("문제 세트 이름"),
                        fieldWithPath("data.description").type(JsonFieldType.STRING).description("문제 세트 설명"),
                        fieldWithPath("data.problems").type(JsonFieldType.ARRAY).description("문제 세트에 포함된 문제"),
                        fieldWithPath("data.problems.[].id").type(JsonFieldType.NUMBER).description("문제 ID"),
                        fieldWithPath("data.problems.[].title").type(JsonFieldType.STRING).description("문제 이름"),
                        fieldWithPath("data.problems.[].tags").type(JsonFieldType.ARRAY).description("문제 태그"),
                        fieldWithPath("data.problems.[].avgScore").type(JsonFieldType.NUMBER).description("문제 평균 점수"),
                        fieldWithPath("data.problems.[].totalSubmission").type(JsonFieldType.NUMBER).description(
                            "문제 제출 수",
                        ),
                        fieldWithPath("data.problems.[].type").type(JsonFieldType.STRING).description("문제 타입"),
                    ),
                ),
            )
    }

    private fun createProblemsetDetailResponseDto() = ProblemSetDetailResponseDto(
        1,
        listOf(
            ProblemResponseDto(1, "test 문제", listOf("DS", "Network"), 10.0, 100, "long"),
            ProblemResponseDto(2, "test 문제", listOf("DS", "Network"), 10.0, 100, "multiple"),
            ProblemResponseDto(3, "test 문제", listOf("DS", "Network"), 10.0, 100, "short"),
        ),
        "test 문제세트",
        "테스트용 문제세트",
    )
}
