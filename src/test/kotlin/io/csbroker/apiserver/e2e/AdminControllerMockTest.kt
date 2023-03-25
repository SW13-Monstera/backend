package io.csbroker.apiserver.e2e

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.GradingStandardType
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.controller.v1.AdminController
import io.csbroker.apiserver.dto.problem.ProblemDeleteRequestDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto.ChoiceData
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetUpsertRequestDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import io.csbroker.apiserver.dto.user.GradingStandardResponseDto
import io.csbroker.apiserver.dto.useranswer.AssignUserAnswerDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerBatchInsertDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerLabelRequestDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerResponseDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerSearchResponseDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerUpsertDto
import io.csbroker.apiserver.model.User
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
import org.junit.jupiter.api.Order
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
import java.time.LocalDateTime
import java.util.UUID

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
    fun `Create Long Problem 200`() {
        // given
        every { problemService.createLongProblem(any(), any()) } returns 1L
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
                        fieldWithPath("standardAnswer").type(JsonFieldType.STRING)
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
        every { problemService.updateLongProblem(any(), any(), any()) } returns 1L
        val longProblemUpsertRequestDto = createLongProblemUpsertRequestDto()

        // when
        val result = mockMvc.body(longProblemUpsertRequestDto)
            .request(Method.PUT, "/api/admin/problems/long/{problem_id}", 1L)

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
                        fieldWithPath("standardAnswer").type(JsonFieldType.STRING)
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
        every { problemService.findLongProblemById(any()) } returns LongProblemResponseDto(
            id = 1L,
            title = "title",
            description = "description",
            standardAnswer = "standardAnswer",
            tags = listOf("tag1", "tag2"),
            gradingStandards = listOf(
                GradingStandardResponseDto(
                    id = 1L,
                    content = "content",
                    score = 1.0,
                    type = GradingStandardType.KEYWORD,
                ),
            ),
            isGradable = true,
            isActive = true,
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/admin/problems/long/{problem_id}", 1L)

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
                        fieldWithPath("data.standardAnswer").type(JsonFieldType.STRING)
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
    fun `Create Short Problem 200`() {
        // give
        val shortProblemUpsertRequestDto = ShortProblemUpsertRequestDto(
            "test",
            "test",
            mutableListOf("db", "network"),
            "test",
            5.0,
        )
        every { problemService.createShortProblem(any(), any()) } returns 1L

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
        every { problemService.updateShortProblem(any(), any(), any()) } returns 1L

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
        every { problemService.findShortProblemById(any()) } returns shortProblemResponseDto

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
        every { problemService.createMultipleChoiceProblem(any(), any()) } returns 1L

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
        every { problemService.updateMultipleChoiceProblem(any(), any(), any()) } returns 1L

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
        every { problemService.findMultipleProblemById(any()) } returns MultipleChoiceProblemResponseDto(
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

    @Test
    fun `Create UserAnswer 200`() {
        // given
        val userAnswerUpsertDto = UserAnswerUpsertDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "test",
            1L,
        )
        every { userAnswerService.createUserAnswer(any()) } returns 1L

        // when
        val result = mockMvc.body(userAnswerUpsertDto).request(Method.POST, "/api/admin/user-answer")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/userAnswer/createOne",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("assignedUserId").type(JsonFieldType.STRING)
                            .description("할당 할 유저 ID ( UUID, null 값 가능 )").optional(),
                        fieldWithPath("validatingUserId").type(JsonFieldType.STRING)
                            .description("검수자로 할당 할 유저 ID ( UUID, null 값 가능. )").optional(),
                        fieldWithPath("answer").type(JsonFieldType.STRING)
                            .description("유저 답안"),
                        fieldWithPath("problemId").type(JsonFieldType.NUMBER)
                            .description("답안에 대한 문제 ID"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("유저 답안 ID"),
                    ),
                ),
            )
    }

    @Test
    fun `Create UserAnswers 200`() {
        // given
        val userAnswerUpsertDto = UserAnswerBatchInsertDto(
            1,
            listOf(
                UserAnswerUpsertDto(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "test",
                    1L,
                ),
            ),
        )
        every { userAnswerService.createUserAnswers(any()) } returns 1

        // when
        val result = mockMvc.body(userAnswerUpsertDto).request(Method.POST, "/api/admin/user-answers")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/userAnswer/createMany",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("size").type(JsonFieldType.NUMBER)
                            .description("생성할 유저 답안 수"),
                        fieldWithPath("userAnswers.[].assignedUserId")
                            .type(JsonFieldType.STRING)
                            .description("할당 할 유저 ID ( UUID, null 값 가능 )").optional(),
                        fieldWithPath("userAnswers.[].validatingUserId")
                            .type(JsonFieldType.STRING)
                            .description("검수자로 할당 할 유저 ID ( UUID, null 값 가능. )").optional(),
                        fieldWithPath("userAnswers.[].answer").type(JsonFieldType.STRING)
                            .description("유저 답안"),
                        fieldWithPath("userAnswers.[].problemId").type(JsonFieldType.NUMBER)
                            .description("답안에 대한 문제 ID"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.size")
                            .type(JsonFieldType.NUMBER).description("생성 된 유저 답안 수"),
                    ),
                ),
            )
    }

    @Test
    fun `Get UserAnswer 200`() {
        // given
        every { userAnswerService.findUserAnswerById(any()) } returns UserAnswerResponseDto(
            1L,
            1L,
            "TEST",
            "TEST",
            "TEST",
            true,
            true,
            listOf(
                GradingStandardResponseDto(
                    1L,
                    "TEST",
                    1.0,
                    GradingStandardType.KEYWORD,
                ),
            ),
            listOf(
                GradingStandardResponseDto(
                    2L,
                    "TEST",
                    1.0,
                    GradingStandardType.CONTENT,
                ),
            ),
            listOf(1L),
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/admin/user-answers/{user_answer_id}", 1L)

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/userAnswer/findOne",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("user_answer_id").description("유저 답안 id"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("유저 답안 ID"),
                        fieldWithPath("data.problemId")
                            .type(JsonFieldType.NUMBER).description("문제 ID"),
                        fieldWithPath("data.problemTitle")
                            .type(JsonFieldType.STRING).description("문제 제목"),
                        fieldWithPath("data.problemDescription")
                            .type(JsonFieldType.STRING).description("문제 내용"),
                        fieldWithPath("data.answer")
                            .type(JsonFieldType.STRING).description("유저 답안"),
                        fieldWithPath("data.isLabeled")
                            .type(JsonFieldType.BOOLEAN).description("유저 답안 라벨링 유무"),
                        fieldWithPath("data.isValidated")
                            .type(JsonFieldType.BOOLEAN).description("유저 답안 검수 유무"),
                        fieldWithPath("data.keywordsGradingStandards")
                            .type(JsonFieldType.ARRAY).description("키워드 채점 기준"),
                        fieldWithPath("data.keywordsGradingStandards.[].id")
                            .type(JsonFieldType.NUMBER).description("키워드 채점기준 ID"),
                        fieldWithPath("data.keywordsGradingStandards.[].content")
                            .type(JsonFieldType.STRING).description("채점기준 내용"),
                        fieldWithPath("data.keywordsGradingStandards.[].score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
                        fieldWithPath("data.keywordsGradingStandards.[].type")
                            .type(JsonFieldType.STRING).description("채점기준 타입 ( 'KEYWORD' )"),
                        fieldWithPath("data.contentGradingStandards")
                            .type(JsonFieldType.ARRAY).description("내용 채점 기준"),
                        fieldWithPath("data.contentGradingStandards.[].id")
                            .type(JsonFieldType.NUMBER).description("내용 채점기준 ID"),
                        fieldWithPath("data.contentGradingStandards.[].content")
                            .type(JsonFieldType.STRING).description("내용 채점기준 내용"),
                        fieldWithPath("data.contentGradingStandards.[].score")
                            .type(JsonFieldType.NUMBER).description("내용 채점기준 점수"),
                        fieldWithPath("data.contentGradingStandards.[].type")
                            .type(JsonFieldType.STRING).description("채점기준 타입 ( 'CONTENT' )"),
                        fieldWithPath("data.selectedGradingStandards")
                            .type(JsonFieldType.ARRAY).description("선택 된 채점 기준 IDs"),
                    ),
                ),
            )
    }

    @Test
    @Order(15)
    fun `Label UserAnswer 200`() {
        // given
        every { userAnswerService.labelUserAnswer(any(), any(), any()) } returns 1L
        val userAnswerLabelRequestDto = UserAnswerLabelRequestDto(
            listOf(1L, 2L),
        )

        // when
        val result = mockMvc.body(userAnswerLabelRequestDto)
            .request(Method.POST, "/api/admin/user-answers/{user_answer_id}/label", 1L)

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/userAnswer/label",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("user_answer_id").description("유저 답안 id"),
                    ),
                    requestFields(
                        fieldWithPath("selectedGradingStandardIds")
                            .type(JsonFieldType.ARRAY)
                            .description("선택한 채점 기준 ID 리스트"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("유저 답안 ID"),
                    ),
                ),
            )
    }

    @Test
    fun `Validate UserAnswer 200`() {
        // given
        every { userAnswerService.validateUserAnswer(any(), any(), any()) } returns 1L
        val userAnswerLabelRequestDto = UserAnswerLabelRequestDto(
            listOf(1L, 2L),
        )

        // when
        val result = mockMvc.body(userAnswerLabelRequestDto)
            .request(Method.POST, "/api/admin/user-answers/{user_answer_id}/validate", 1L)

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/userAnswer/validate",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("user_answer_id").description("유저 답안 id"),
                    ),
                    requestFields(
                        fieldWithPath("selectedGradingStandardIds")
                            .type(JsonFieldType.ARRAY)
                            .description("선택한 채점 기준 ID 리스트"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("유저 답안 ID"),
                    ),
                ),
            )
    }

    @Test
    fun `Search LongProblem 200`() {
        // given
        every { problemService.findLongProblems(any(), any(), any(), any()) } returns LongProblemSearchResponseDto(
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

    @Test
    fun `Search ShortProblem 200`() {
        // given
        every { problemService.findShortProblems(any(), any(), any(), any()) } returns ShortProblemSearchResponseDto(
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

    @Test
    fun `Search MultipleChoiceProblem 200`() {
        // given
        every {
            problemService.findMultipleProblems(
                any(),
                any(),
                any(),
                any(),
            )
        } returns MultipleChoiceProblemSearchResponseDto(
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

    @Test
    fun `Search UserAnswers 200`() {
        // given
        every {
            userAnswerService.findUserAnswersByQuery(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns UserAnswerSearchResponseDto(
            listOf(
                UserAnswerSearchResponseDto.UserAnswerDataDto(
                    1,
                    "title",
                    "assignor",
                    "validator",
                    LocalDateTime.now(),
                    true,
                    true,
                ),
            ),
            1,
            1,
        )

        val assignedBy = "assignor"
        val validatedBy = "validator"
        val problemTitle = "t"
        val answer = "t"
        val isLabeled = false
        val isValidated = false
        val size = 5
        val page = 0

        // when
        val result = mockMvc.request(
            Method.GET,
            "/api/admin/user-answers?assignedBy=$assignedBy&validatedBy=$validatedBy&problemTitle=" +
                "$problemTitle&answer=$answer&isLabeled=$isLabeled&isValidated=$isValidated&size=$size&page=$page",
        )

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/userAnswer/search",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestParameters(
                        parameterWithName("id")
                            .description("ID 검색 ( 옵션 )").optional(),
                        parameterWithName("assignedBy")
                            .description("할당된 유저의 닉네임 ( 옵션 )").optional(),
                        parameterWithName("validatedBy")
                            .description("검수자로 할당된 유저의 닉네임 ( 옵션 )").optional(),
                        parameterWithName("problemTitle")
                            .description("답안을 작성한 문제의 제목 ( 옵션 )").optional(),
                        parameterWithName("answer")
                            .description("유저 답안 내용 ( 옵션 )"),
                        parameterWithName("isLabeled")
                            .description("라벨링 여부 ( 옵션 )"),
                        parameterWithName("isValidated")
                            .description("검수 여부 ( 옵션 )"),
                        parameterWithName("size")
                            .description("한 페이지당 가져올 개수 ( 옵션 )"),
                        parameterWithName("page")
                            .description("페이지 ( 0부터 시작, 옵션 )").optional(),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.userAnswers").type(JsonFieldType.ARRAY)
                            .description("유저 답안 데이터"),
                        fieldWithPath("data.userAnswers.[].id").type(JsonFieldType.NUMBER)
                            .description("유저 답안 id"),
                        fieldWithPath("data.userAnswers.[].problemTitle")
                            .type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.userAnswers.[].assignedUsername")
                            .type(JsonFieldType.STRING)
                            .description("할당 된 유저 닉네임 ( null 가능 ) ").optional(),
                        fieldWithPath("data.userAnswers.[].validatingUsername")
                            .type(JsonFieldType.STRING)
                            .description("검수자로 할당 된 유저 닉네임 ( null 가능 ) ").optional(),
                        fieldWithPath("data.userAnswers.[].updatedAt")
                            .type(JsonFieldType.STRING)
                            .description("수정된 날짜"),
                        fieldWithPath("data.userAnswers.[].isLabeled")
                            .type(JsonFieldType.BOOLEAN)
                            .description("라벨링 여부"),
                        fieldWithPath("data.userAnswers.[].isValidated")
                            .type(JsonFieldType.BOOLEAN)
                            .description("검수 여부"),
                        fieldWithPath("data.totalPages")
                            .type(JsonFieldType.NUMBER)
                            .description("총 페이지 수"),
                        fieldWithPath("data.totalElements")
                            .type(JsonFieldType.NUMBER)
                            .description("검색된 총 유저 응답 수"),
                    ),
                ),
            )
    }

    @Test
    fun `Search Admin Users 200`() {
        // given
        every { userService.findAdminUsers() } returns listOf(
            User(
                UUID.randomUUID(),
                "nickname",
                "email",
                "password",
                Role.ROLE_ADMIN,
                ProviderType.LOCAL,
                "blogUrl",
                "description",
            ),
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/admin/users/admin")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/user/admin/search",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING)
                            .description("결과 상태"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY)
                            .description("ADMIN 유저 정보"),
                        fieldWithPath("data.[].id").type(JsonFieldType.STRING)
                            .description("유저 ID"),
                        fieldWithPath("data.[].username")
                            .type(JsonFieldType.STRING)
                            .description("유저 닉네임"),
                    ),
                ),
            )
    }

    @Test
    fun `Assign Labeler to User Answer 200`() {
        // given
        every { userAnswerService.assignLabelUserAnswer(any(), any()) } returns Unit
        val assignUserAnswerDto = AssignUserAnswerDto(
            listOf(1L, 2L),
            UUID.randomUUID(),
        )

        // when
        val result = mockMvc.body(assignUserAnswerDto).request(Method.PUT, "/api/admin/user-answers/assign/label")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/userAnswer/assign/label",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("userAnswerIds")
                            .type(JsonFieldType.ARRAY).description("유저 답안 id 리스트"),
                        fieldWithPath("assigneeId")
                            .type(JsonFieldType.STRING).description("할당 할 ADMIN 유저 id"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.size")
                            .type(JsonFieldType.NUMBER).description("업데이트 된 유저 답안 size"),
                    ),
                ),
            )
    }

    @Test
    fun `Assign Validator to User Answer 200`() {
        // given
        every { userAnswerService.assignValidationUserAnswer(any(), any()) } returns Unit
        val assignUserAnswerDto = AssignUserAnswerDto(
            listOf(1L, 2L),
            UUID.randomUUID(),
        )

        // when
        val result = mockMvc.body(assignUserAnswerDto).request(Method.PUT, "/api/admin/user-answers/assign/validate")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/userAnswer/assign/validate",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("userAnswerIds")
                            .type(JsonFieldType.ARRAY).description("유저 답안 id 리스트"),
                        fieldWithPath("assigneeId")
                            .type(JsonFieldType.STRING).description("할당 할 ADMIN 유저 id"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.size")
                            .type(JsonFieldType.NUMBER).description("업데이트 된 유저 답안 size"),
                    ),
                ),
            )
    }

    @Test
    fun `Delete User Answer By Id 200`() {
        // given
        every { userAnswerService.removeUserAnswerById(any()) } returns Unit

        // when
        val result = mockMvc.request(Method.DELETE, "/api/admin/user-answers/{user_answer_id}", 1L)

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/userAnswer/deleteOne",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("user_answer_id").description("유저 응답 id"),
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

    private fun createLongProblemUpsertRequestDto() = LongProblemUpsertRequestDto(
        "test",
        "test",
        "test",
        mutableListOf("db", "network"),
        mutableListOf(
            LongProblemUpsertRequestDto.GradingStandardData(
                "keyword-1",
                1.0,
                GradingStandardType.KEYWORD,
            ),
        ),
    )
}
