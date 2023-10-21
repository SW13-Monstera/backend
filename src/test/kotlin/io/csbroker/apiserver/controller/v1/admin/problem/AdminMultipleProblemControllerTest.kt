package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto.ChoiceData
import io.csbroker.apiserver.service.problem.admin.AdminMultipleProblemService
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

class AdminMultipleProblemControllerTest : RestDocsTest() {
    private lateinit var problemService: AdminMultipleProblemService
    private lateinit var mockMvc: MockMvcRequestSpecification

    @BeforeEach
    fun setUp() {
        problemService = mockk()
        mockMvc = mockMvc(
            AdminMultipleProblemController(problemService),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `Create Multiple Problem 200`() {
        // given
        val multipleChoiceProblemUpsertRequestDto = MultipleChoiceProblemUpsertRequestDto(
            "test",
            "test",
            mutableListOf("db", "network"),
            mutableListOf(
                ChoiceData(
                    "choice-1",
                    true,
                ),
                ChoiceData(
                    "choice-2",
                    false,
                ),
            ),
            5.0,
        )
        every { problemService.createProblem(any(), any()) } returns 1L

        // when
        val result =
            mockMvc.body(multipleChoiceProblemUpsertRequestDto).request(Method.POST, "/api/admin/problems/multiple")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/multiple/create",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        fieldWithPath("tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        fieldWithPath("choices").type(JsonFieldType.ARRAY)
                            .description("선지"),
                        fieldWithPath("choices.[].content").type(JsonFieldType.STRING)
                            .description("선지 내용"),
                        fieldWithPath("choices.[].isAnswer").type(JsonFieldType.BOOLEAN)
                            .description("선지 정답 여부"),
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
    fun `Update Multiple Problem 200`() {
        // given
        val multipleChoiceProblemUpsertRequestDto = MultipleChoiceProblemUpsertRequestDto(
            "test",
            "test",
            mutableListOf("db", "network"),
            mutableListOf(
                ChoiceData(
                    "choice-1",
                    true,
                ),
                ChoiceData(
                    "choice-2",
                    false,
                ),
            ),
            5.0,
        )
        every { problemService.updateProblem(any(), any()) } returns 1L

        // when
        val result = mockMvc.body(multipleChoiceProblemUpsertRequestDto)
            .request(Method.PUT, "/api/admin/problems/multiple/{problem_id}", 1L)

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/multiple/modify",
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
                        fieldWithPath("choices").type(JsonFieldType.ARRAY)
                            .description("선지"),
                        fieldWithPath("choices.[].content").type(JsonFieldType.STRING)
                            .description("선지 내용"),
                        fieldWithPath("choices.[].isAnswer").type(JsonFieldType.BOOLEAN)
                            .description("선지 정답 여부"),
                        fieldWithPath("score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
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
    fun `Get Multiple Problem By Id 200`() {
        // given
        every { problemService.findProblemById(any()) } returns MultipleChoiceProblemResponseDto(
            1L,
            "test",
            "test",
            mutableListOf("db", "network"),
            true,
            mutableListOf(
                ChoiceData(
                    "choice-1",
                    true,
                ),
                ChoiceData(
                    "choice-2",
                    false,
                ),
            ),
            5.0,
            true,
            true,
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/admin/problems/multiple/{problem_id}", 1L)

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/multiple/findOne",
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
                        fieldWithPath("data.isMultiple").type(JsonFieldType.BOOLEAN)
                            .description("다중 정답 유무"),
                        fieldWithPath("data.choiceData").type(JsonFieldType.ARRAY)
                            .description("선지 데이터"),
                        fieldWithPath("data.choiceData.[].content")
                            .type(JsonFieldType.STRING).description("선지 내용"),
                        fieldWithPath("data.choiceData.[].isAnswer")
                            .type(JsonFieldType.BOOLEAN).description("선지 정답 여부"),
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
    fun `Search MultipleChoiceProblem 200`() {
        // given
        every { problemService.findProblems(any()) } returns MultipleChoiceProblemSearchResponseDto(
            listOf(
                MultipleChoiceProblemSearchResponseDto.MultipleChoiceProblemDataDto(
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
            "/api/admin/problems/multiple?title=$title&description=$description&size=$size&page=$page",
        )

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/problems/multiple/search",
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
