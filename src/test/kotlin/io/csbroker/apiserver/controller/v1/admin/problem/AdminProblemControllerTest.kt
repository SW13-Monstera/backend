package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.problem.ProblemDeleteRequestDto
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetUpsertRequestDto
import io.csbroker.apiserver.service.problem.CommonProblemService
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
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters

class AdminProblemControllerTest : RestDocsTest() {
    private lateinit var problemService: CommonProblemService
    private lateinit var problemSetService: ProblemSetService
    private lateinit var mockMvc: MockMvcRequestSpecification

    @BeforeEach
    fun setUp() {
        problemService = mockk()
        problemSetService = mockk()
        mockMvc = mockMvc(
            AdminProblemController(problemService, problemSetService),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `문제 세트 생성`() {
        // given
        every { problemSetService.createProblemSet(any()) } returns 1L

        // when
        val response = mockMvc.header("Authorization", "Bearer some-token")
            .body(ProblemSetUpsertRequestDto(listOf(1L, 2L), "name", "description"))
            .request(Method.POST, "/api/admin/problem-sets")

        // then
        response.then().statusCode(200)
            .apply(
                document(
                    "problem-sets-create",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("problemIds").type(JsonFieldType.ARRAY).description("문제 ID 목록"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("문제 세트 이름"),
                        fieldWithPath("description").type(JsonFieldType.STRING).description("문제 세트 설명"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER).description("문제 세트 ID"),
                    ),
                ),
            )
    }

    @Test
    fun `문제 세트 수정`() {
        // given
        every { problemSetService.updateProblemSet(any(), any()) } returns 1L

        // when
        val response = mockMvc
            .body(ProblemSetUpsertRequestDto(listOf(1L, 2L), "name", "description"))
            .request(Method.PUT, "/api/admin/problem-sets/{problem_set_id}", 1L)

        // then
        response.then().statusCode(200)
            .apply(
                document(
                    "problem-sets-update",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("problem_set_id").description("문제 세트 id"),
                    ),
                    requestFields(
                        fieldWithPath("problemIds").type(JsonFieldType.ARRAY).description("문제 ID 목록"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("문제 세트 이름"),
                        fieldWithPath("description").type(JsonFieldType.STRING).description("문제 세트 설명"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER).description("문제 세트 ID"),
                    ),
                ),
            )
    }

    @Test
    fun `Delete Problem By Id 200`() {
        // given
        every { problemService.removeProblemById(any()) } returns Unit

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
        every { problemService.removeProblemsById(any()) } returns Unit

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
