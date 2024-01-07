package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.common.enums.GradingStandardType
import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.dto.user.GradingStandardResponseDto
import io.csbroker.apiserver.service.problem.admin.AdminLongProblemService
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
import org.springframework.restdocs.request.RequestDocumentation.queryParameters

class AdminLongProblemControllerTest : RestDocsTest() {
    private lateinit var problemService: AdminLongProblemService
    private lateinit var mockMvc: MockMvcRequestSpecification

    @BeforeEach
    fun setUp() {
        problemService = mockk()
        mockMvc = mockMvc(
            AdminLongProblemController(problemService),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `Create Long Problem 200`() {
        // given
        every { problemService.createProblem(any(), any()) } returns 1L
        val longProblemUpsertRequestDto = createLongProblemUpsertRequestDto()

        // when
        val result = mockMvc.body(longProblemUpsertRequestDto).request(Method.POST, "/api/admin/problems/long")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/long/create",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        fieldWithPath("standardAnswers").type(JsonFieldType.ARRAY)
                            .description("모범 답안"),
                        fieldWithPath("tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        fieldWithPath("gradingStandards").type(JsonFieldType.ARRAY)
                            .description("채점기준"),
                        fieldWithPath("gradingStandards.[].content")
                            .type(JsonFieldType.STRING).description("채점기준 내용"),
                        fieldWithPath("gradingStandards.[].score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
                        fieldWithPath("gradingStandards.[].type")
                            .type(JsonFieldType.STRING).description("채점기준 타입 ( 'KEYWORD' or 'CONTENT' )"),
                        fieldWithPath("isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부 ( 필수 x, 기본 값 false )"),
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
    fun `Update Long Problem 200`() {
        // given
        every { problemService.updateProblem(any(), any()) } returns 1L
        val longProblemUpsertRequestDto = createLongProblemUpsertRequestDto()

        // when
        val result = mockMvc.body(longProblemUpsertRequestDto)
            .request(Method.PUT, "/api/admin/problems/long/{problem_id}", "1")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/long/modify",
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
                        fieldWithPath("standardAnswers").type(JsonFieldType.ARRAY)
                            .description("모범 답안"),
                        fieldWithPath("tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        fieldWithPath("gradingStandards").type(JsonFieldType.ARRAY)
                            .description("채점기준"),
                        fieldWithPath("gradingStandards.[].content")
                            .type(JsonFieldType.STRING).description("채점기준 내용"),
                        fieldWithPath("gradingStandards.[].score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
                        fieldWithPath("gradingStandards.[].type")
                            .type(JsonFieldType.STRING).description("채점기준 타입 ( 'KEYWORD' or 'CONTENT' )"),
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
    fun `Get Long Problem By Id 200`() {
        // given
        every { problemService.findProblemById(any()) } returns LongProblemResponseDto(
            id = 1L,
            title = "title",
            standardAnswers = listOf("answer"),
            description = "description",
            tags = listOf("tag1", "tag2"),
            gradingStandards = listOf(
                GradingStandardResponseDto(
                    id = 1L,
                    content = "content",
                    score = 1.0,
                    type = GradingStandardType.KEYWORD,
                ),
            ),
            isActive = true,
            isGradable = true,
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/admin/problems/long/{problem_id}", "1")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/long/findOne",
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
                        fieldWithPath("data.standardAnswers").type(JsonFieldType.ARRAY)
                            .description("모범 답안"),
                        fieldWithPath("data.tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        fieldWithPath("data.gradingStandards").type(JsonFieldType.ARRAY)
                            .description("채점기준"),
                        fieldWithPath("data.gradingStandards.[].id")
                            .type(JsonFieldType.NUMBER).description("채점기준 ID"),
                        fieldWithPath("data.gradingStandards.[].content")
                            .type(JsonFieldType.STRING).description("채점기준 내용"),
                        fieldWithPath("data.gradingStandards.[].score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
                        fieldWithPath("data.gradingStandards.[].type")
                            .type(JsonFieldType.STRING).description("채점기준 타입 ( 'KEYWORD' or 'CONTENT' )"),
                        fieldWithPath("data.isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부"),
                        fieldWithPath("data.isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부"),
                    ),
                ),
            )
    }

    @Test
    fun `Search LongProblem 200`() {
        // given
        every { problemService.findProblems(any()) } returns LongProblemSearchResponseDto(
            listOf(
                LongProblemSearchResponseDto.LongProblemDataDto(
                    1,
                    "title",
                    "creator",
                    1.0,
                    1.0,
                    1,
                    true,
                ),
            ),
            1,
            1,
        )
        val title = "title"
        val description = "description"
        val size = 10
        val page = 0

        // when
        val result = mockMvc.request(
            Method.GET,
            "/api/admin/problems/long?title=$title&description=$description&size=$size&page=$page",
        )

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/long/search",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    queryParameters(
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
                        fieldWithPath("data.problems.[].avgKeywordScore")
                            .type(JsonFieldType.NUMBER)
                            .description("평균 키워드 점수 ( 푼 사람이 없으면 null )").optional(),
                        fieldWithPath("data.problems.[].avgContentScore")
                            .type(JsonFieldType.NUMBER)
                            .description("평균 내용 점수 ( 푼 사람이 없으면 null )").optional(),
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

    private fun createLongProblemUpsertRequestDto() = LongProblemUpsertRequestDto(
        "test",
        "test",
        listOf("test"),
        listOf("db", "network"),
        listOf(
            LongProblemUpsertRequestDto.GradingStandardData(
                "keyword-1",
                1.0,
                GradingStandardType.KEYWORD,
            ),
        ),
    )
}
