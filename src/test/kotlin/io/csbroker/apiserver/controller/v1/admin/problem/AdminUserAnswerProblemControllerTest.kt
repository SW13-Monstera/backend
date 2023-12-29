package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.user.GradingStandardResponseDto
import io.csbroker.apiserver.dto.useranswer.AssignUserAnswerDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerBatchInsertDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerLabelRequestDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerResponseDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerSearchResponseDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerUpsertDto
import io.csbroker.apiserver.service.problem.UserAnswerService
import io.mockk.every
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
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import java.time.LocalDateTime
import java.util.UUID

class AdminUserAnswerProblemControllerTest : RestDocsTest() {
    private lateinit var userAnswerService: UserAnswerService
    private lateinit var mockMvc: MockMvcRequestSpecification

    @BeforeEach
    fun setUp() {
        userAnswerService = mockk()
        mockMvc = mockMvc(
            AdminUserAnswerController(userAnswerService),
        ).header("Authorization", "Bearer TEST-TOKEN")
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
        val result = mockMvc.request(Method.GET, "/api/admin/user-answers/{user_answer_id}", "1")

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
    fun `Label UserAnswer 200`() {
        // given
        every { userAnswerService.labelUserAnswer(any(), any(), any()) } returns 1L
        val userAnswerLabelRequestDto = UserAnswerLabelRequestDto(
            listOf(1L, 2L),
        )

        // when
        val result = mockMvc.body(userAnswerLabelRequestDto)
            .request(Method.POST, "/api/admin/user-answers/{user_answer_id}/label", "1")

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
            .request(Method.POST, "/api/admin/user-answers/{user_answer_id}/validate", "1")

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
                    queryParameters(
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
    fun `Assign Labeler to User Answer 200`() {
        // given
        justRun { userAnswerService.assignLabelUserAnswer(any(), any()) }
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
        justRun { userAnswerService.assignValidationUserAnswer(any(), any()) }
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
        justRun { userAnswerService.removeUserAnswerById(any()) }

        // when
        val result = mockMvc.request(Method.DELETE, "/api/admin/user-answers/{user_answer_id}", "1")

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
}
