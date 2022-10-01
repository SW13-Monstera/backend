package io.csbroker.apiserver.e2e

import io.csbroker.apiserver.auth.AuthTokenProvider
import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.GradingStandardType
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.dto.useranswer.UserAnswerBatchInsertDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerLabelRequestDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerUpsertDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import io.csbroker.apiserver.dto.problem.ProblemDeleteRequestDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import io.csbroker.apiserver.dto.useranswer.AssignUserAnswerDto
import io.csbroker.apiserver.model.Tag
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.LongProblemRepository
import io.csbroker.apiserver.repository.MultipleChoiceProblemRepository
import io.csbroker.apiserver.repository.ShortProblemRepository
import io.csbroker.apiserver.repository.TagRepository
import io.csbroker.apiserver.repository.UserAnswerRepository
import io.csbroker.apiserver.repository.UserRepository
import io.csbroker.apiserver.service.ProblemService
import io.csbroker.apiserver.service.UserAnswerService
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.Date

@SpringBootTest
@AutoConfigureRestDocs
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var tagRepository: TagRepository

    @Autowired
    private lateinit var longProblemRepository: LongProblemRepository

    @Autowired
    private lateinit var multipleChoiceProblemRepository: MultipleChoiceProblemRepository

    @Autowired
    private lateinit var shortProblemRepository: ShortProblemRepository

    @Autowired
    private lateinit var userAnswerRepository: UserAnswerRepository

    @Autowired
    private lateinit var problemService: ProblemService

    @Autowired
    private lateinit var userAnswerService: UserAnswerService

    @Autowired
    private lateinit var tokenProvider: io.csbroker.apiserver.auth.AuthTokenProvider

    private lateinit var user: User

    private lateinit var token: String

    private val ADMIN_ENDPOINT = "/api/admin"

    @BeforeAll
    fun setup() {
        val user = User(
            email = "test-admin2@test.com",
            username = "test-admin2",
            providerType = ProviderType.LOCAL,
            role = Role.ROLE_ADMIN
        )

        userRepository.save(user)

        this.user = user

        val osTag = Tag(
            name = "network"
        )
        tagRepository.save(osTag)

        val dsTag = Tag(
            name = "db"
        )
        tagRepository.save(dsTag)

        val now = Date()

        val accessToken = tokenProvider.createAuthToken(
            "test-admin2@test.com",
            expiry = Date(now.time + 6000000),
            role = Role.ROLE_ADMIN.code
        )

        this.token = accessToken.token
    }

    @Test
    @Order(1)
    fun `Create Long Problem 200`() {
        // given
        val longProblemUpsertRequestDto = LongProblemUpsertRequestDto(
            "test",
            """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
            """.trimIndent(),
            """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
            """.trimIndent(),
            mutableListOf("db", "network"),
            mutableListOf(
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-1",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-2",
                    3.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-3",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-1",
                    2.0,
                    GradingStandardType.PROMPT
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-2",
                    3.0,
                    GradingStandardType.PROMPT
                )
            )
        )

        val upsertDtoString = this.objectMapper.writeValueAsString(longProblemUpsertRequestDto)

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("$ADMIN_ENDPOINT/problems/long")
                .contentType(MediaType.APPLICATION_JSON)
                .content(upsertDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/long/create",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        PayloadDocumentation.fieldWithPath("standardAnswer").type(JsonFieldType.STRING)
                            .description("모범 답안"),
                        PayloadDocumentation.fieldWithPath("tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        PayloadDocumentation.fieldWithPath("gradingStandards").type(JsonFieldType.ARRAY)
                            .description("채점기준"),
                        PayloadDocumentation.fieldWithPath("gradingStandards.[].content")
                            .type(JsonFieldType.STRING).description("채점기준 내용"),
                        PayloadDocumentation.fieldWithPath("gradingStandards.[].score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
                        PayloadDocumentation.fieldWithPath("gradingStandards.[].type")
                            .type(JsonFieldType.STRING).description("채점기준 타입 ( 'KEYWORD' or 'PROMPT' )"),
                        PayloadDocumentation.fieldWithPath("isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부 ( 필수 x, 기본 값 false )"),
                        PayloadDocumentation.fieldWithPath("isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부 ( 필수 x, 기본 값 true )")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("문제 ID")
                    )
                )
            )
    }

    @Test
    @Order(2)
    fun `Update Long Problem 200`() {
        // given
        val problemInsertDto = LongProblemUpsertRequestDto(
            "test",
            """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
            """.trimIndent(),
            """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
            """.trimIndent(),
            mutableListOf("db"),
            mutableListOf(
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-1",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-2",
                    3.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-3",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-1",
                    2.0,
                    GradingStandardType.PROMPT
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-2",
                    3.0,
                    GradingStandardType.PROMPT
                )
            )
        )

        val id = problemService.createLongProblem(problemInsertDto, "test-admin2@test.com")

        val longProblemUpsertRequestDto = LongProblemUpsertRequestDto(
            "test",
            """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
            """.trimIndent(),
            """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
            """.trimIndent(),
            mutableListOf("db"),
            mutableListOf(
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-1",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-2",
                    3.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-3",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-1",
                    2.0,
                    GradingStandardType.PROMPT
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-2",
                    3.0,
                    GradingStandardType.PROMPT
                )
            )
        )

        val upsertDtoString = this.objectMapper.writeValueAsString(longProblemUpsertRequestDto)

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.put(
                "$ADMIN_ENDPOINT/problems/long/{problem_id}",
                id
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(upsertDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/long/modify",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("problem_id").description("문제 id")
                    ),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        PayloadDocumentation.fieldWithPath("standardAnswer").type(JsonFieldType.STRING)
                            .description("모범 답안"),
                        PayloadDocumentation.fieldWithPath("tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        PayloadDocumentation.fieldWithPath("gradingStandards").type(JsonFieldType.ARRAY)
                            .description("채점기준"),
                        PayloadDocumentation.fieldWithPath("gradingStandards.[].content")
                            .type(JsonFieldType.STRING).description("채점기준 내용"),
                        PayloadDocumentation.fieldWithPath("gradingStandards.[].score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
                        PayloadDocumentation.fieldWithPath("gradingStandards.[].type")
                            .type(JsonFieldType.STRING).description("채점기준 타입 ( 'KEYWORD' or 'PROMPT' )"),
                        PayloadDocumentation.fieldWithPath("isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부"),
                        PayloadDocumentation.fieldWithPath("isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("문제 ID")
                    )
                )
            )
    }

    @Test
    @Order(3)
    fun `Get Long Problem By Id 200`() {
        // given
        val problemInsertDto = LongProblemUpsertRequestDto(
            "test",
            """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
            """.trimIndent(),
            """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
            """.trimIndent(),
            mutableListOf("db"),
            mutableListOf(
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-1",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-2",
                    3.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-3",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-1",
                    2.0,
                    GradingStandardType.PROMPT
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-2",
                    3.0,
                    GradingStandardType.PROMPT
                )
            )
        )

        val id = problemService.createLongProblem(problemInsertDto, "test-admin2@test.com")

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get("$ADMIN_ENDPOINT/problems/long/{problem_id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/long/findOne",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("problem_id").description("문제 id")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("문제 ID"),
                        PayloadDocumentation.fieldWithPath("data.title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("data.description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        PayloadDocumentation.fieldWithPath("data.standardAnswer").type(JsonFieldType.STRING)
                            .description("모범 답안"),
                        PayloadDocumentation.fieldWithPath("data.tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        PayloadDocumentation.fieldWithPath("data.gradingStandards").type(JsonFieldType.ARRAY)
                            .description("채점기준"),
                        PayloadDocumentation.fieldWithPath("data.gradingStandards.[].id")
                            .type(JsonFieldType.NUMBER).description("채점기준 ID"),
                        PayloadDocumentation.fieldWithPath("data.gradingStandards.[].content")
                            .type(JsonFieldType.STRING).description("채점기준 내용"),
                        PayloadDocumentation.fieldWithPath("data.gradingStandards.[].score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
                        PayloadDocumentation.fieldWithPath("data.gradingStandards.[].type")
                            .type(JsonFieldType.STRING).description("채점기준 타입 ( 'KEYWORD' or 'PROMPT' )"),
                        PayloadDocumentation.fieldWithPath("data.isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부"),
                        PayloadDocumentation.fieldWithPath("data.isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부")
                    )
                )
            )
    }

    @Test
    @Order(4)
    fun `Create Short Problem 200`() {
        // give
        val shortProblemUpsertRequestDto = ShortProblemUpsertRequestDto(
            "test",
            "test",
            mutableListOf("db", "network"),
            "test",
            5.0
        )

        val upsertDtoString = this.objectMapper.writeValueAsString(shortProblemUpsertRequestDto)

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("$ADMIN_ENDPOINT/problems/short")
                .contentType(MediaType.APPLICATION_JSON)
                .content(upsertDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/short/create",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        PayloadDocumentation.fieldWithPath("tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        PayloadDocumentation.fieldWithPath("answer").type(JsonFieldType.STRING)
                            .description("정답"),
                        PayloadDocumentation.fieldWithPath("score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
                        PayloadDocumentation.fieldWithPath("isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부 ( 필수 x, 기본 값 true )"),
                        PayloadDocumentation.fieldWithPath("isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부 ( 필수 x, 기본 값 true )")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("문제 ID")
                    )
                )
            )
    }

    @Test
    @Order(5)
    fun `Update Short Problem 200`() {
        // given
        val shortProblemUpsertRequestDto = ShortProblemUpsertRequestDto(
            "test1",
            "test1",
            mutableListOf("db", "network"),
            "test",
            5.0
        )

        val id = this.problemService.createShortProblem(shortProblemUpsertRequestDto, "test-admin2@test.com")

        val upsertDtoString = this.objectMapper.writeValueAsString(shortProblemUpsertRequestDto)

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.put("$ADMIN_ENDPOINT/problems/short/{problem_id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(upsertDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/short/modify",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("problem_id").description("문제 id")
                    ),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        PayloadDocumentation.fieldWithPath("tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        PayloadDocumentation.fieldWithPath("answer").type(JsonFieldType.STRING)
                            .description("문제 정답"),
                        PayloadDocumentation.fieldWithPath("score")
                            .type(JsonFieldType.NUMBER).description("문제 점수"),
                        PayloadDocumentation.fieldWithPath("isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부"),
                        PayloadDocumentation.fieldWithPath("isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("문제 ID")
                    )
                )
            )
    }

    @Test
    @Order(6)
    fun `Get Short Problem By Id 200`() {
        // given
        val shortProblemUpsertRequestDto = ShortProblemUpsertRequestDto(
            "test1",
            "test1",
            mutableListOf("db", "network"),
            "test",
            5.0
        )

        val id = this.problemService.createShortProblem(shortProblemUpsertRequestDto, "test-admin2@test.com")

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get("$ADMIN_ENDPOINT/problems/short/{problem_id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/short/findOne",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("problem_id").description("문제 id")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("문제 ID"),
                        PayloadDocumentation.fieldWithPath("data.title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("data.description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        PayloadDocumentation.fieldWithPath("data.tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        PayloadDocumentation.fieldWithPath("data.answer").type(JsonFieldType.STRING)
                            .description("문제 정답"),
                        PayloadDocumentation.fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("문제 점수"),
                        PayloadDocumentation.fieldWithPath("data.isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부"),
                        PayloadDocumentation.fieldWithPath("data.isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부")
                    )
                )
            )
    }

    @Test
    @Order(7)
    fun `Create Multiple Problem 200`() {
        // given
        val multipleChoiceProblemUpsertRequestDto = MultipleChoiceProblemUpsertRequestDto(
            "test",
            "test",
            mutableListOf("db", "network"),
            mutableListOf(
                MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                    "choice-1",
                    true
                ),
                MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                    "choice-2",
                    false
                )
            ),
            5.0
        )

        val upsertDtoString = this.objectMapper.writeValueAsString(multipleChoiceProblemUpsertRequestDto)

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("$ADMIN_ENDPOINT/problems/multiple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(upsertDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/multiple/create",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        PayloadDocumentation.fieldWithPath("tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        PayloadDocumentation.fieldWithPath("choices").type(JsonFieldType.ARRAY)
                            .description("선지"),
                        PayloadDocumentation.fieldWithPath("choices.[].content").type(JsonFieldType.STRING)
                            .description("선지 내용"),
                        PayloadDocumentation.fieldWithPath("choices.[].isAnswer").type(JsonFieldType.BOOLEAN)
                            .description("선지 정답 여부"),
                        PayloadDocumentation.fieldWithPath("score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
                        PayloadDocumentation.fieldWithPath("isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부 ( 필수 x, 기본 값 true )"),
                        PayloadDocumentation.fieldWithPath("isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부 ( 필수 x, 기본 값 true )")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("문제 ID")
                    )
                )
            )
    }

    @Test
    @Order(8)
    fun `Update Multiple Problem 200`() {
        // given
        val multipleChoiceProblemUpsertRequestDto = MultipleChoiceProblemUpsertRequestDto(
            "test",
            "test",
            mutableListOf("db", "network"),
            mutableListOf(
                MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                    "choice-1",
                    true
                ),
                MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                    "choice-2",
                    false
                )
            ),
            5.0
        )

        val id = this.problemService.createMultipleChoiceProblem(
            multipleChoiceProblemUpsertRequestDto,
            "test-admin2@test.com"
        )

        val upsertDtoString = this.objectMapper.writeValueAsString(multipleChoiceProblemUpsertRequestDto)

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.put("$ADMIN_ENDPOINT/problems/multiple/{problem_id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(upsertDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/multiple/modify",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("problem_id").description("문제 id")
                    ),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        PayloadDocumentation.fieldWithPath("tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        PayloadDocumentation.fieldWithPath("choices").type(JsonFieldType.ARRAY)
                            .description("선지"),
                        PayloadDocumentation.fieldWithPath("choices.[].content").type(JsonFieldType.STRING)
                            .description("선지 내용"),
                        PayloadDocumentation.fieldWithPath("choices.[].isAnswer").type(JsonFieldType.BOOLEAN)
                            .description("선지 정답 여부"),
                        PayloadDocumentation.fieldWithPath("score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
                        PayloadDocumentation.fieldWithPath("isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부"),
                        PayloadDocumentation.fieldWithPath("isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("문제 ID")
                    )
                )
            )
    }

    @Test
    @Order(9)
    fun `Get Multiple Problem By Id 200`() {
        // given
        val multipleChoiceProblemUpsertRequestDto = MultipleChoiceProblemUpsertRequestDto(
            "test",
            "test",
            mutableListOf("db", "network"),
            mutableListOf(
                MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                    "choice-1",
                    true
                ),
                MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                    "choice-2",
                    false
                )
            ),
            5.0
        )

        val id = this.problemService.createMultipleChoiceProblem(
            multipleChoiceProblemUpsertRequestDto,
            "test-admin2@test.com"
        )

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get("$ADMIN_ENDPOINT/problems/multiple/{problem_id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/multiple/findOne",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("problem_id").description("문제 id")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("문제 ID"),
                        PayloadDocumentation.fieldWithPath("data.title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("data.description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        PayloadDocumentation.fieldWithPath("data.tags").type(JsonFieldType.ARRAY)
                            .description("태그"),
                        PayloadDocumentation.fieldWithPath("data.isMultiple").type(JsonFieldType.BOOLEAN)
                            .description("다중 정답 유무"),
                        PayloadDocumentation.fieldWithPath("data.choiceData").type(JsonFieldType.ARRAY)
                            .description("선지 데이터"),
                        PayloadDocumentation.fieldWithPath("data.choiceData.[].content")
                            .type(JsonFieldType.STRING).description("선지 내용"),
                        PayloadDocumentation.fieldWithPath("data.choiceData.[].isAnswer")
                            .type(JsonFieldType.BOOLEAN).description("선지 정답 여부"),
                        PayloadDocumentation.fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("문제 점수"),
                        PayloadDocumentation.fieldWithPath("data.isGradable")
                            .type(JsonFieldType.BOOLEAN).description("채점 가능 여부"),
                        PayloadDocumentation.fieldWithPath("data.isActive")
                            .type(JsonFieldType.BOOLEAN).description("활성화 여부")
                    )
                )
            )
    }

    @Test
    @Order(10)
    fun `Delete Problem By Id 200`() {
        // given
        val multipleChoiceProblemUpsertRequestDto = MultipleChoiceProblemUpsertRequestDto(
            "test",
            "test",
            mutableListOf("db", "network"),
            mutableListOf(
                MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                    "choice-1",
                    true
                ),
                MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                    "choice-2",
                    false
                )
            ),
            5.0
        )

        val id = this.problemService.createMultipleChoiceProblem(
            multipleChoiceProblemUpsertRequestDto,
            "test-admin2@test.com"
        )

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.delete("$ADMIN_ENDPOINT/problems/{problem_id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/deleteOne",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("problem_id").description("문제 id")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data")
                            .type(JsonFieldType.BOOLEAN).description("성공 유무 ( 삭제 성공시 true를 return )")
                    )
                )
            )
    }

    @Test
    @Order(11)
    fun `Delete Problems By Ids 200`() {
        // given
        val ids = mutableListOf<Long>()

        for (i in 1..10) {
            val multipleChoiceProblemUpsertRequestDto = MultipleChoiceProblemUpsertRequestDto(
                "test$i",
                "test$i",
                mutableListOf("db", "network"),
                mutableListOf(
                    MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                        "choice-1",
                        true
                    ),
                    MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                        "choice-2",
                        false
                    )
                ),
                5.0
            )

            val id = this.problemService.createMultipleChoiceProblem(
                multipleChoiceProblemUpsertRequestDto,
                "test-admin2@test.com"
            )

            ids.add(id)
        }

        val problemDeleteRequestDtoString = this.objectMapper.writeValueAsString(ProblemDeleteRequestDto(ids))

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.delete("$ADMIN_ENDPOINT/problems")
                .contentType(MediaType.APPLICATION_JSON)
                .content(problemDeleteRequestDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/deleteMany",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("ids").type(JsonFieldType.ARRAY)
                            .description("삭제 할 문제 id 리스트")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data")
                            .type(JsonFieldType.BOOLEAN).description("성공 유무 ( 삭제 성공시 true를 return )")
                    )
                )
            )
    }

    @Test
    @Order(12)
    fun `Create UserAnswer 200`() {
        // given
        val problemInsertDto = LongProblemUpsertRequestDto(
            "test",
            """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
            """.trimIndent(),
            """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
            """.trimIndent(),
            mutableListOf("db"),
            mutableListOf(
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-1",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-2",
                    3.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-3",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-1",
                    2.0,
                    GradingStandardType.PROMPT
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-2",
                    3.0,
                    GradingStandardType.PROMPT
                )
            )
        )

        val id = problemService.createLongProblem(problemInsertDto, "test-admin2@test.com")

        val userAnswerUpsertDto = UserAnswerUpsertDto(
            user.id!!,
            user.id!!,
            "test",
            id
        )

        val userAnswerUpsertDtoString = this.objectMapper.writeValueAsString(userAnswerUpsertDto)

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.post("$ADMIN_ENDPOINT/user-answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userAnswerUpsertDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/userAnswer/createOne",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("assignedUserId").type(JsonFieldType.STRING)
                            .description("할당 할 유저 ID ( UUID, null 값 가능 )").optional(),
                        PayloadDocumentation.fieldWithPath("validatingUserId").type(JsonFieldType.STRING)
                            .description("검수자로 할당 할 유저 ID ( UUID, null 값 가능. )").optional(),
                        PayloadDocumentation.fieldWithPath("answer").type(JsonFieldType.STRING)
                            .description("유저 답안"),
                        PayloadDocumentation.fieldWithPath("problemId").type(JsonFieldType.NUMBER)
                            .description("답안에 대한 문제 ID")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("유저 답안 ID")
                    )
                )
            )
    }

    @Test
    @Order(13)
    fun `Create UserAnswers 200`() {
        // given
        val problemInsertDto = LongProblemUpsertRequestDto(
            "test",
            """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
            """.trimIndent(),
            """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
            """.trimIndent(),
            mutableListOf("db"),
            mutableListOf(
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-1",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-2",
                    3.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-3",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-1",
                    2.0,
                    GradingStandardType.PROMPT
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-2",
                    3.0,
                    GradingStandardType.PROMPT
                )
            )
        )

        val id = problemService.createLongProblem(problemInsertDto, "test-admin2@test.com")

        val userAnswerUpsertDtoList = (1..10).map {
            UserAnswerUpsertDto(
                user.id!!,
                user.id!!,
                "test$it",
                id
            )
        }.toList()

        val userAnswerBatchInsertDto = UserAnswerBatchInsertDto(
            10,
            userAnswerUpsertDtoList
        )

        val userAnswerBatchInsertDtoString = this.objectMapper.writeValueAsString(userAnswerBatchInsertDto)

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.post("$ADMIN_ENDPOINT/user-answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userAnswerBatchInsertDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/userAnswer/createMany",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("size").type(JsonFieldType.NUMBER)
                            .description("생성할 유저 답안 수"),
                        PayloadDocumentation.fieldWithPath("userAnswers.[].assignedUserId")
                            .type(JsonFieldType.STRING)
                            .description("할당 할 유저 ID ( UUID, null 값 가능 )").optional(),
                        PayloadDocumentation.fieldWithPath("userAnswers.[].validatingUserId")
                            .type(JsonFieldType.STRING)
                            .description("검수자로 할당 할 유저 ID ( UUID, null 값 가능. )").optional(),
                        PayloadDocumentation.fieldWithPath("userAnswers.[].answer").type(JsonFieldType.STRING)
                            .description("유저 답안"),
                        PayloadDocumentation.fieldWithPath("userAnswers.[].problemId").type(JsonFieldType.NUMBER)
                            .description("답안에 대한 문제 ID")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.size")
                            .type(JsonFieldType.NUMBER).description("생성 된 유저 답안 수")
                    )
                )
            )
    }

    @Test
    @Order(14)
    fun `Get UserAnswer 200`() {
        // given
        val problemInsertDto = LongProblemUpsertRequestDto(
            "test",
            """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
            """.trimIndent(),
            """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
            """.trimIndent(),
            mutableListOf("db"),
            mutableListOf(
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-1",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-2",
                    3.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-3",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-1",
                    2.0,
                    GradingStandardType.PROMPT
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-2",
                    3.0,
                    GradingStandardType.PROMPT
                )
            )
        )

        val problemId = problemService.createLongProblem(problemInsertDto, "test-admin2@test.com")

        val userAnswerUpsertDto = UserAnswerUpsertDto(
            user.id!!,
            user.id!!,
            "test",
            problemId
        )

        val id = this.userAnswerService.createUserAnswer(userAnswerUpsertDto)

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get("$ADMIN_ENDPOINT/user-answers/{user_answer_id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/userAnswer/findOne",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("user_answer_id").description("유저 답안 id")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("유저 답안 ID"),
                        PayloadDocumentation.fieldWithPath("data.problemId")
                            .type(JsonFieldType.NUMBER).description("문제 ID"),
                        PayloadDocumentation.fieldWithPath("data.problemTitle")
                            .type(JsonFieldType.STRING).description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("data.problemDescription")
                            .type(JsonFieldType.STRING).description("문제 내용"),
                        PayloadDocumentation.fieldWithPath("data.answer")
                            .type(JsonFieldType.STRING).description("유저 답안"),
                        PayloadDocumentation.fieldWithPath("data.isLabeled")
                            .type(JsonFieldType.BOOLEAN).description("유저 답안 라벨링 유무"),
                        PayloadDocumentation.fieldWithPath("data.isValidated")
                            .type(JsonFieldType.BOOLEAN).description("유저 답안 검수 유무"),
                        PayloadDocumentation.fieldWithPath("data.keywordsGradingStandards")
                            .type(JsonFieldType.ARRAY).description("키워드 채점 기준"),
                        PayloadDocumentation.fieldWithPath("data.keywordsGradingStandards.[].id")
                            .type(JsonFieldType.NUMBER).description("키워드 채점기준 ID"),
                        PayloadDocumentation.fieldWithPath("data.keywordsGradingStandards.[].content")
                            .type(JsonFieldType.STRING).description("채점기준 내용"),
                        PayloadDocumentation.fieldWithPath("data.keywordsGradingStandards.[].score")
                            .type(JsonFieldType.NUMBER).description("채점기준 점수"),
                        PayloadDocumentation.fieldWithPath("data.keywordsGradingStandards.[].type")
                            .type(JsonFieldType.STRING).description("채점기준 타입 ( 'KEYWORD' )"),
                        PayloadDocumentation.fieldWithPath("data.promptGradingStandards")
                            .type(JsonFieldType.ARRAY).description("내용 채점 기준"),
                        PayloadDocumentation.fieldWithPath("data.promptGradingStandards.[].id")
                            .type(JsonFieldType.NUMBER).description("내용 채점기준 ID"),
                        PayloadDocumentation.fieldWithPath("data.promptGradingStandards.[].content")
                            .type(JsonFieldType.STRING).description("내용 채점기준 내용"),
                        PayloadDocumentation.fieldWithPath("data.promptGradingStandards.[].score")
                            .type(JsonFieldType.NUMBER).description("내용 채점기준 점수"),
                        PayloadDocumentation.fieldWithPath("data.promptGradingStandards.[].type")
                            .type(JsonFieldType.STRING).description("채점기준 타입 ( 'PROMPT' )"),
                        PayloadDocumentation.fieldWithPath("data.selectedGradingStandards")
                            .type(JsonFieldType.ARRAY).description("선택 된 채점 기준 IDs")
                    )
                )
            )
    }

    @Test
    @Order(15)
    fun `Label UserAnswer 200`() {
        // given
        val problemInsertDto = LongProblemUpsertRequestDto(
            "test",
            """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
            """.trimIndent(),
            """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
            """.trimIndent(),
            mutableListOf("db"),
            mutableListOf(
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-1",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-2",
                    3.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-3",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-1",
                    2.0,
                    GradingStandardType.PROMPT
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-2",
                    3.0,
                    GradingStandardType.PROMPT
                )
            )
        )

        val problemId = problemService.createLongProblem(problemInsertDto, "test-admin2@test.com")

        val gradingStandards = problemService.findLongProblemById(problemId).gradingStandards.map {
            it.id
        }

        val userAnswerUpsertDto = UserAnswerUpsertDto(
            user.id!!,
            user.id!!,
            "test",
            problemId
        )

        val id = this.userAnswerService.createUserAnswer(userAnswerUpsertDto)

        val userAnswerLabelRequestDto = UserAnswerLabelRequestDto(
            gradingStandards
        )

        val userAnswerLabelRequestDtoString = this.objectMapper.writeValueAsString(userAnswerLabelRequestDto)

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.post(
                "$ADMIN_ENDPOINT/user-answers/{user_answer_id}/label",
                id
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(userAnswerLabelRequestDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/userAnswer/label",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("user_answer_id").description("유저 답안 id")
                    ),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("selectedGradingStandardIds")
                            .type(JsonFieldType.ARRAY)
                            .description("선택한 채점 기준 ID 리스트")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("유저 답안 ID")
                    )
                )
            )
    }

    @Test
    @Order(16)
    fun `Validate UserAnswer 200`() {
        // given
        val problemInsertDto = LongProblemUpsertRequestDto(
            "test",
            """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
            """.trimIndent(),
            """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
            """.trimIndent(),
            mutableListOf("db"),
            mutableListOf(
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-1",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-2",
                    3.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-3",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-1",
                    2.0,
                    GradingStandardType.PROMPT
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-2",
                    3.0,
                    GradingStandardType.PROMPT
                )
            )
        )

        val problemId = problemService.createLongProblem(problemInsertDto, "test-admin2@test.com")

        val gradingStandards = problemService.findLongProblemById(problemId).gradingStandards.map {
            it.id
        }

        val userAnswerUpsertDto = UserAnswerUpsertDto(
            user.id!!,
            user.id!!,
            "test",
            problemId
        )

        val id = this.userAnswerService.createUserAnswer(userAnswerUpsertDto)

        this.userAnswerService.labelUserAnswer("test-admin2@test.com", id, gradingStandards)

        val userAnswerLabelRequestDto = UserAnswerLabelRequestDto(
            gradingStandards
        )

        val userAnswerLabelRequestDtoString = this.objectMapper.writeValueAsString(userAnswerLabelRequestDto)

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.post(
                "$ADMIN_ENDPOINT/user-answers/{user_answer_id}/validate",
                id
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(userAnswerLabelRequestDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/userAnswer/validate",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("user_answer_id").description("유저 답안 id")
                    ),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("selectedGradingStandardIds")
                            .type(JsonFieldType.ARRAY)
                            .description("선택한 채점 기준 ID 리스트")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("유저 답안 ID")
                    )
                )
            )
    }

    @Test
    @Order(17)
    fun `Search LongProblem 200`() {
        // given
        for (i in 1..10) {
            val problemInsertDto = LongProblemUpsertRequestDto(
                "test",
                """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
                """.trimIndent(),
                """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
                """.trimIndent(),
                mutableListOf("db"),
                mutableListOf(
                    LongProblemUpsertRequestDto.GradingStandardData(
                        "keyword-1",
                        1.0,
                        GradingStandardType.KEYWORD
                    ),
                    LongProblemUpsertRequestDto.GradingStandardData(
                        "keyword-2",
                        3.0,
                        GradingStandardType.KEYWORD
                    ),
                    LongProblemUpsertRequestDto.GradingStandardData(
                        "keyword-3",
                        1.0,
                        GradingStandardType.KEYWORD
                    ),
                    LongProblemUpsertRequestDto.GradingStandardData(
                        "prompt-1",
                        2.0,
                        GradingStandardType.PROMPT
                    ),
                    LongProblemUpsertRequestDto.GradingStandardData(
                        "prompt-2",
                        3.0,
                        GradingStandardType.PROMPT
                    )
                )
            )
            this.problemService.createLongProblem(problemInsertDto, "test-admin2@test.com")
        }

        val title = "test"
        val description = "a"
        val size = 5
        val page = 1

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(
                "$ADMIN_ENDPOINT/problems/long?" +
                    "title=$title&description=$description&size=$size&page=$page"
            )
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/long/search",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.requestParameters(
                        RequestDocumentation.parameterWithName("id")
                            .description("ID 검색 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("title")
                            .description("제목 검색 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("description")
                            .description("문제 설명 검색 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("size")
                            .description("한 페이지당 가져올 개수 ( 옵션 )"),
                        RequestDocumentation.parameterWithName("page")
                            .description("페이지 ( 0부터 시작, 옵션 )").optional(),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.problems").type(JsonFieldType.ARRAY)
                            .description("문제 데이터"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].id").type(JsonFieldType.NUMBER)
                            .description("문제 id"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].creator").type(JsonFieldType.STRING)
                            .description("문제 제작자 닉네임"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].avgKeywordScore")
                            .type(JsonFieldType.NUMBER)
                            .description("평균 키워드 점수 ( 푼 사람이 없으면 null )").optional(),
                        PayloadDocumentation.fieldWithPath("data.problems.[].avgPromptScore")
                            .type(JsonFieldType.NUMBER)
                            .description("평균 내용 점수 ( 푼 사람이 없으면 null )").optional(),
                        PayloadDocumentation.fieldWithPath("data.problems.[].userAnswerCnt")
                            .type(JsonFieldType.NUMBER)
                            .description("제출된 답안 수"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].isActive")
                            .type(JsonFieldType.BOOLEAN)
                            .description("활성화 여부"),
                        PayloadDocumentation.fieldWithPath("data.totalPages")
                            .type(JsonFieldType.NUMBER)
                            .description("총 페이지 수"),
                        PayloadDocumentation.fieldWithPath("data.totalElements")
                            .type(JsonFieldType.NUMBER)
                            .description("검색된 총 문제수")
                    )
                )
            )
    }

    @Test
    @Order(18)
    fun `Search ShortProblem 200`() {
        // given
        for (i in 1..10) {
            val shortProblemUpsertRequestDto = ShortProblemUpsertRequestDto(
                "test$i",
                "test$i",
                mutableListOf("db", "network"),
                "test$i",
                5.0
            )

            this.problemService.createShortProblem(shortProblemUpsertRequestDto, "test-admin2@test.com")
        }

        val title = "test"
        val description = "t"
        val size = 5
        val page = 1

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(
                "$ADMIN_ENDPOINT/problems/short?" +
                    "title=$title&description=$description&size=$size&page=$page"
            )
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/short/search",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.requestParameters(
                        RequestDocumentation.parameterWithName("id")
                            .description("ID 검색 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("title")
                            .description("제목 검색 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("description")
                            .description("문제 설명 검색 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("size")
                            .description("한 페이지당 가져올 개수 ( 옵션 )"),
                        RequestDocumentation.parameterWithName("page")
                            .description("페이지 ( 0부터 시작, 옵션 )").optional(),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.problems").type(JsonFieldType.ARRAY)
                            .description("문제 데이터"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].id").type(JsonFieldType.NUMBER)
                            .description("문제 id"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].creator").type(JsonFieldType.STRING)
                            .description("문제 제작자 닉네임"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].answerRate")
                            .type(JsonFieldType.NUMBER)
                            .description("정답률 ( 푼 사람이 없으면 null )").optional(),
                        PayloadDocumentation.fieldWithPath("data.problems.[].userAnswerCnt")
                            .type(JsonFieldType.NUMBER)
                            .description("제출된 답안 수"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].isActive")
                            .type(JsonFieldType.BOOLEAN)
                            .description("활성화 여부"),
                        PayloadDocumentation.fieldWithPath("data.totalPages")
                            .type(JsonFieldType.NUMBER)
                            .description("총 페이지 수"),
                        PayloadDocumentation.fieldWithPath("data.totalElements")
                            .type(JsonFieldType.NUMBER)
                            .description("검색된 총 문제수")
                    )
                )
            )
    }

    @Test
    @Order(19)
    fun `Search MultipleChoiceProblem 200`() {
        // given
        for (i in 1..10) {
            val multipleChoiceProblemUpsertRequestDto = MultipleChoiceProblemUpsertRequestDto(
                "test$i",
                "test$i",
                mutableListOf("db", "network"),
                mutableListOf(
                    MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                        "choice-1",
                        true
                    ),
                    MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                        "choice-2",
                        false
                    )
                ),
                5.0
            )

            this.problemService.createMultipleChoiceProblem(
                multipleChoiceProblemUpsertRequestDto,
                "test-admin2@test.com"
            )
        }

        val title = "test"
        val description = "t"
        val size = 5
        val page = 1

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(
                "$ADMIN_ENDPOINT/problems/multiple?" +
                    "title=$title&description=$description&size=$size&page=$page"
            )
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/problems/multiple/search",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.requestParameters(
                        RequestDocumentation.parameterWithName("id")
                            .description("ID 검색 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("title")
                            .description("제목 검색 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("description")
                            .description("문제 설명 검색 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("size")
                            .description("한 페이지당 가져올 개수 ( 옵션 )"),
                        RequestDocumentation.parameterWithName("page")
                            .description("페이지 ( 0부터 시작, 옵션 )").optional(),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.problems").type(JsonFieldType.ARRAY)
                            .description("문제 데이터"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].id").type(JsonFieldType.NUMBER)
                            .description("문제 id"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].creator").type(JsonFieldType.STRING)
                            .description("문제 제작자 닉네임"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].answerRate")
                            .type(JsonFieldType.NUMBER)
                            .description("정답률 ( 푼 사람이 없으면 null )").optional(),
                        PayloadDocumentation.fieldWithPath("data.problems.[].userAnswerCnt")
                            .type(JsonFieldType.NUMBER)
                            .description("제출된 답안 수"),
                        PayloadDocumentation.fieldWithPath("data.problems.[].isActive")
                            .type(JsonFieldType.BOOLEAN)
                            .description("활성화 여부"),
                        PayloadDocumentation.fieldWithPath("data.totalPages")
                            .type(JsonFieldType.NUMBER)
                            .description("총 페이지 수"),
                        PayloadDocumentation.fieldWithPath("data.totalElements")
                            .type(JsonFieldType.NUMBER)
                            .description("검색된 총 문제수")
                    )
                )
            )
    }

    @Test
    @Order(20)
    fun `Search UserAnswers 200`() {
        // given
        val problemInsertDto = LongProblemUpsertRequestDto(
            "test",
            """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
            """.trimIndent(),
            """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
            """.trimIndent(),
            mutableListOf("db"),
            mutableListOf(
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-1",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-2",
                    3.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-3",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-1",
                    2.0,
                    GradingStandardType.PROMPT
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-2",
                    3.0,
                    GradingStandardType.PROMPT
                )
            )
        )

        val problemId = problemService.createLongProblem(problemInsertDto, "test-admin2@test.com")

        for (i in 1..10) {
            val userAnswerUpsertDto = UserAnswerUpsertDto(
                user.id!!,
                user.id!!,
                "test",
                problemId
            )

            this.userAnswerService.createUserAnswer(userAnswerUpsertDto)
        }

        val assignedBy = "test-admin2"
        val validatedBy = "test-admin2"
        val problemTitle = "t"
        val answer = "t"
        val isLabeled = false
        val isValidated = false
        val size = 5
        val page = 0

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(
                "$ADMIN_ENDPOINT/user-answers?" +
                    "assignedBy=$assignedBy&validatedBy=$validatedBy&problemTitle=$problemTitle&" +
                    "answer=$answer&isLabeled=$isLabeled&isValidated=$isValidated&size=$size&page=$page"
            )
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/userAnswer/search",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.requestParameters(
                        RequestDocumentation.parameterWithName("id")
                            .description("ID 검색 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("assignedBy")
                            .description("할당된 유저의 닉네임 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("validatedBy")
                            .description("검수자로 할당된 유저의 닉네임 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("problemTitle")
                            .description("답안을 작성한 문제의 제목 ( 옵션 )").optional(),
                        RequestDocumentation.parameterWithName("answer")
                            .description("유저 답안 내용 ( 옵션 )"),
                        RequestDocumentation.parameterWithName("isLabeled")
                            .description("라벨링 여부 ( 옵션 )"),
                        RequestDocumentation.parameterWithName("isValidated")
                            .description("검수 여부 ( 옵션 )"),
                        RequestDocumentation.parameterWithName("size")
                            .description("한 페이지당 가져올 개수 ( 옵션 )"),
                        RequestDocumentation.parameterWithName("page")
                            .description("페이지 ( 0부터 시작, 옵션 )").optional(),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.userAnswers").type(JsonFieldType.ARRAY)
                            .description("유저 답안 데이터"),
                        PayloadDocumentation.fieldWithPath("data.userAnswers.[].id").type(JsonFieldType.NUMBER)
                            .description("유저 답안 id"),
                        PayloadDocumentation.fieldWithPath("data.userAnswers.[].problemTitle")
                            .type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        PayloadDocumentation.fieldWithPath("data.userAnswers.[].assignedUsername")
                            .type(JsonFieldType.STRING)
                            .description("할당 된 유저 닉네임 ( null 가능 ) ").optional(),
                        PayloadDocumentation.fieldWithPath("data.userAnswers.[].validatingUsername")
                            .type(JsonFieldType.STRING)
                            .description("검수자로 할당 된 유저 닉네임 ( null 가능 ) ").optional(),
                        PayloadDocumentation.fieldWithPath("data.userAnswers.[].updatedAt")
                            .type(JsonFieldType.STRING)
                            .description("수정된 날짜"),
                        PayloadDocumentation.fieldWithPath("data.userAnswers.[].isLabeled")
                            .type(JsonFieldType.BOOLEAN)
                            .description("라벨링 여부"),
                        PayloadDocumentation.fieldWithPath("data.userAnswers.[].isValidated")
                            .type(JsonFieldType.BOOLEAN)
                            .description("검수 여부"),
                        PayloadDocumentation.fieldWithPath("data.totalPages")
                            .type(JsonFieldType.NUMBER)
                            .description("총 페이지 수"),
                        PayloadDocumentation.fieldWithPath("data.totalElements")
                            .type(JsonFieldType.NUMBER)
                            .description("검색된 총 유저 응답 수")
                    )
                )
            )
    }

    @Test
    @Order(21)
    fun `Search Admin Users 200`() {
        // given
        val accessToken = token

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(
                "$ADMIN_ENDPOINT/users/admin"
            )
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/user/admin/search",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status").type(JsonFieldType.STRING)
                            .description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data").type(JsonFieldType.ARRAY)
                            .description("ADMIN 유저 정보"),
                        PayloadDocumentation.fieldWithPath("data.[].id").type(JsonFieldType.STRING)
                            .description("유저 ID"),
                        PayloadDocumentation.fieldWithPath("data.[].username")
                            .type(JsonFieldType.STRING)
                            .description("유저 닉네임")
                    )
                )
            )
    }

    @Test
    @Order(22)
    fun `Assign Labeler to User Answer 200`() {
        // given
        val userAnswerIds = mutableListOf<Long>()
        val problemInsertDto = LongProblemUpsertRequestDto(
            "test",
            """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
            """.trimIndent(),
            """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
            """.trimIndent(),
            mutableListOf("db"),
            mutableListOf(
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-1",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-2",
                    3.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-3",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-1",
                    2.0,
                    GradingStandardType.PROMPT
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-2",
                    3.0,
                    GradingStandardType.PROMPT
                )
            )
        )

        val problemId = problemService.createLongProblem(problemInsertDto, "test-admin2@test.com")

        for (i in 1..10) {
            val userAnswerUpsertDto = UserAnswerUpsertDto(
                user.id!!,
                user.id!!,
                "test",
                problemId
            )

            val userAnswerId = this.userAnswerService.createUserAnswer(userAnswerUpsertDto)

            userAnswerIds.add(userAnswerId)
        }

        val assignUserAnswerDto = AssignUserAnswerDto(userAnswerIds, user.id!!)
        val assignUserAnswerDtoStr = objectMapper.writeValueAsString(assignUserAnswerDto)

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.put("$ADMIN_ENDPOINT/user-answers/assign/label")
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignUserAnswerDtoStr)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/userAnswer/assign/label",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("userAnswerIds")
                            .type(JsonFieldType.ARRAY).description("유저 답안 id 리스트"),
                        PayloadDocumentation.fieldWithPath("assigneeId")
                            .type(JsonFieldType.STRING).description("할당 할 ADMIN 유저 id")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.size")
                            .type(JsonFieldType.NUMBER).description("업데이트 된 유저 답안 size")
                    )
                )
            )
    }

    @Test
    @Order(23)
    fun `Assign Validator to User Answer 200`() {
        // given
        val userAnswerIds = mutableListOf<Long>()
        val problemInsertDto = LongProblemUpsertRequestDto(
            "test",
            """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry.
                Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
                when an unknown printer took a galley of type and scrambled it to make a type specimen book.
                It has survived not only five centuries, but also the leap into electronic typesetting,
                remaining essentially unchanged. It was popularised in the 1960s with the release of
                Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software
                like Aldus PageMaker including versions of Lorem Ipsum.
            """.trimIndent(),
            """
                It is a long established fact that a reader will be distracted by the readable content of
                a page when looking at its layout.
            """.trimIndent(),
            mutableListOf("db"),
            mutableListOf(
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-1",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-2",
                    3.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "keyword-3",
                    1.0,
                    GradingStandardType.KEYWORD
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-1",
                    2.0,
                    GradingStandardType.PROMPT
                ),
                LongProblemUpsertRequestDto.GradingStandardData(
                    "prompt-2",
                    3.0,
                    GradingStandardType.PROMPT
                )
            )
        )

        val problemId = problemService.createLongProblem(problemInsertDto, "test-admin2@test.com")

        for (i in 1..10) {
            val userAnswerUpsertDto = UserAnswerUpsertDto(
                user.id!!,
                user.id!!,
                "test",
                problemId
            )

            val userAnswerId = this.userAnswerService.createUserAnswer(userAnswerUpsertDto)

            userAnswerIds.add(userAnswerId)
        }

        val assignUserAnswerDto = AssignUserAnswerDto(userAnswerIds, user.id!!)
        val assignUserAnswerDtoStr = objectMapper.writeValueAsString(assignUserAnswerDto)

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.put("$ADMIN_ENDPOINT/user-answers/assign/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignUserAnswerDtoStr)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/userAnswer/assign/validate",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("userAnswerIds")
                            .type(JsonFieldType.ARRAY).description("유저 답안 id 리스트"),
                        PayloadDocumentation.fieldWithPath("assigneeId")
                            .type(JsonFieldType.STRING).description("할당 할 ADMIN 유저 id")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.size")
                            .type(JsonFieldType.NUMBER).description("업데이트 된 유저 답안 size")
                    )
                )
            )
    }
}
