package io.csbroker.apiserver.e2e

import io.csbroker.apiserver.controller.v1.AdminController
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetUpsertRequestDto
import io.csbroker.apiserver.service.NotificationService
import io.csbroker.apiserver.service.ProblemService
import io.csbroker.apiserver.service.ProblemSetService
import io.csbroker.apiserver.service.UserAnswerService
import io.csbroker.apiserver.service.UserService
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

class AdminControllerMockTest : RestDocsTest() {
    private lateinit var problemService: ProblemService
    private lateinit var userAnswerService: UserAnswerService
    private lateinit var userService: UserService
    private lateinit var notificationService: NotificationService
    private lateinit var problemSetService: ProblemSetService
    private lateinit var mockMvc: MockMvcRequestSpecification

    @BeforeEach
    fun setUp() {
        problemService = mockk()
        userAnswerService = mockk()
        userService = mockk()
        notificationService = mockk()
        problemSetService = mockk()
        mockMvc = mockMvc(
            AdminController(problemService, userAnswerService, userService, notificationService, problemSetService),
        )
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
        val response = mockMvc.header("Authorization", "Bearer some-token")
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
}
