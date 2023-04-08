package io.csbroker.apiserver.e2e.admin.problem

import io.csbroker.apiserver.controller.v1.admin.problem.AdminShortProblemController
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import io.csbroker.apiserver.e2e.RestDocsTest
import io.csbroker.apiserver.service.problem.AdminShortProblemService
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
import org.springframework.restdocs.request.RequestDocumentation.requestParameters

class AdminShortProblemControllerTest : RestDocsTest() {
    private lateinit var problemService: AdminShortProblemService
    private lateinit var mockMvc: MockMvcRequestSpecification

    @BeforeEach
    fun setUp() {
        problemService = mockk()
        mockMvc = mockMvc(
            AdminShortProblemController(problemService),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `Create Short Problem 200`() {
        // give
        val shortProblemUpsertRequestDto = ShortProblemUpsertRequestDto(
            "test",
            "test",
            mutableListOf("db", "network"),
            "test",
            5.0,
        )
        every { problemService.createProblem(any(), any()) } returns 1L

        // when
        val result = mockMvc.body(shortProblemUpsertRequestDto).request(Method.POST, "/api/admin/problems/short")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/short/create",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        fieldWithPath("tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        fieldWithPath("answer").type(JsonFieldType.STRING)
                            .description("정답"),
                        fieldWithPath("score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
                        fieldWithPath("isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부 ( 필수 x, 기본 값 true )"),
                        fieldWithPath("isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부 ( 필수 x, 기본 값 true )"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("문제 ID"),
                    ),
                ),
            )
    }

    @Test
    fun `Update Short Problem 200`() {
        // given
        val shortProblemUpsertRequestDto = ShortProblemUpsertRequestDto(
            "test1",
            "test1",
            mutableListOf("db", "network"),
            "test",
            5.0,
            true,
            true,
        )
        every { problemService.updateProblem(any(), any(), any()) } returns 1L

        // when
        val result = mockMvc.body(shortProblemUpsertRequestDto)
            .request(Method.PUT, "/api/admin/problems/short/{problem_id}", 1L)

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/short/modify",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("problem_id").description("문제 id"),
                    ),
                    requestFields(
                        fieldWithPath("title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        fieldWithPath("tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        fieldWithPath("answer").type(JsonFieldType.STRING)
                            .description("문제 정답"),
                        fieldWithPath("score")
                            .type(JsonFieldType.NUMBER).description("문제 점수"),
                        fieldWithPath("isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부"),
                        fieldWithPath("isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("문제 ID"),
                    ),
                ),
            )
    }

    @Test
    fun `Get Short Problem By Id 200`() {
        // given
        val shortProblemResponseDto = ShortProblemResponseDto(
            1L,
            "test",
            "test",
            mutableListOf("db", "network"),
            "test",
            5.0,
            true,
            true,
        )
        every { problemService.findProblemById(any()) } returns shortProblemResponseDto

        // when
        val result = mockMvc.request(Method.GET, "/api/admin/problems/short/{problem_id}", 1L)

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/short/findOne",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("problem_id").description("문제 id"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("문제 ID"),
                        fieldWithPath("data.title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        fieldWithPath("data.tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        fieldWithPath("data.answer").type(JsonFieldType.STRING)
                            .description("문제 정답"),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("문제 점수"),
                        fieldWithPath("data.isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부"),
                        fieldWithPath("data.isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부"),
                    ),
                ),
            )
    }

    @Test
    fun `Search ShortProblem 200`() {
        // given
        every { problemService.findProblems(any()) } returns ShortProblemSearchResponseDto(
            listOf(
                ShortProblemSearchResponseDto.ShortProblemDataDto(
                    1,
                    "title",
                    "creator",
                    1.0,
                    1,
                    true,
                ),
            ),
            1,
            1,
        )

        val title = "test"
        val description = "t"
        val size = 5
        val page = 1

        // when
        val result = mockMvc.request(
            Method.GET,
            "/api/admin/problems/short?title=$title&description=$description&size=$size&page=$page",
        )

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/short/search",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestParameters(
                        parameterWithName("id")
                            .description("ID 검색 ( 옵션 )").optional(),
                        parameterWithName("title")
                            .description("제목 검색 ( 옵션 )").optional(),
                        parameterWithName("description")
                            .description("문제 설명 검색 ( 옵션 )").optional(),
                        parameterWithName("size")
                            .description("한 페이지당 가져올 개수 ( 옵션 )"),
                        parameterWithName("page")
                            .description("페이지 ( 0부터 시작, 옵션 )").optional(),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.problems").type(JsonFieldType.ARRAY)
                            .description("문제 데이터"),
                        fieldWithPath("data.problems.[].id").type(JsonFieldType.NUMBER)
                            .description("문제 id"),
                        fieldWithPath("data.problems.[].title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.problems.[].creator").type(JsonFieldType.STRING)
                            .description("문제 제작자 닉네임"),
                        fieldWithPath("data.problems.[].answerRate")
                            .type(JsonFieldType.NUMBER)
                            .description("정답률 ( 푼 사람이 없으면 null )").optional(),
                        fieldWithPath("data.problems.[].userAnswerCnt")
                            .type(JsonFieldType.NUMBER)
                            .description("제출된 답안 수"),
                        fieldWithPath("data.problems.[].isActive")
                            .type(JsonFieldType.BOOLEAN)
                            .description("활성화 여부"),
                        fieldWithPath("data.totalPages")
                            .type(JsonFieldType.NUMBER)
                            .description("총 페이지 수"),
                        fieldWithPath("data.totalElements")
                            .type(JsonFieldType.NUMBER)
                            .description("검색된 총 문제수"),
                    ),
                ),
            )
    }
}
