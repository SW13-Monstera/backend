package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.problem.ProblemDeleteRequestDto
import io.csbroker.apiserver.service.problem.CommonProblemService
import io.mockk.justRun
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
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters

class AdminProblemControllerTest : RestDocsTest() {
    private lateinit var problemService: CommonProblemService
    private lateinit var mockMvc: MockMvcRequestSpecification

    @BeforeEach
    fun setUp() {
        problemService = mockk()
        mockMvc = mockMvc(
            AdminProblemController(problemService),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `Delete Problem By Id 200`() {
        // given
        justRun { problemService.removeProblemById(any()) }

        // when
        val result = mockMvc.request(Method.DELETE, "/api/admin/problems/{problem_id}", 1L)

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/deleteOne",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("problem_id").description("문제 id"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data")
                            .type(JsonFieldType.BOOLEAN).description("성공 유무 ( 삭제 성공시 true를 return )"),
                    ),
                ),
            )
    }

    @Test
    fun `Delete Problems By Ids 200`() {
        // given
        val problemDeleteRequestDto = ProblemDeleteRequestDto(
            mutableListOf(1L, 2L, 3L),
        )
        justRun { problemService.removeProblemsById(any()) }

        // when
        val result = mockMvc.body(problemDeleteRequestDto).request(Method.DELETE, "/api/admin/problems")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/deleteMany",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("ids").type(JsonFieldType.ARRAY)
                            .description("삭제 할 문제 id 리스트"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data")
                            .type(JsonFieldType.BOOLEAN).description("성공 유무 ( 삭제 성공시 true를 return )"),
                    ),
                ),
            )
    }
}
