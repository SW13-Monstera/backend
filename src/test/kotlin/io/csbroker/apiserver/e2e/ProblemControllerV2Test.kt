package io.csbroker.apiserver.e2e

import com.fasterxml.jackson.databind.ObjectMapper
import io.csbroker.apiserver.auth.AuthTokenProvider
import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.controller.v2.request.ChallengeCreateRequest
import io.csbroker.apiserver.model.ShortProblem
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.ProblemRepository
import io.csbroker.apiserver.repository.UserRepository
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
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.util.Date

@SpringBootTest
@AutoConfigureRestDocs
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class ProblemControllerV2Test {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var problemRepository: ProblemRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var tokenProvider: AuthTokenProvider

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val PROBLEM_ENDPOINT = "/api/v2/problems"

    private var adminUser: User? = null

    @BeforeAll
    fun setUpData() {
        val user = User(
            email = "test50@test.com",
            username = "test50",
            providerType = ProviderType.LOCAL
        )

        userRepository.save(user)

        adminUser = user
    }

    @Test
    @Order(1)
    fun `Short problem 단건 조회`() {
        // given
        val shortProblem = ShortProblem(
            title = "test11",
            description = "test",
            creator = adminUser!!,
            answer = "test",
            score = 5.0,
        )

        problemRepository.save(shortProblem)

        val urlString = "$PROBLEM_ENDPOINT/short/{problem_id}"

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(urlString, shortProblem.id)
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                document(
                    "problems/v2/short/inquire",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("problem_id").description("문제 id")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("문제 id"),
                        fieldWithPath("data.title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        fieldWithPath("data.tags").type(JsonFieldType.ARRAY).description("태그"),
                        fieldWithPath("data.correctUserCnt").type(JsonFieldType.NUMBER)
                            .description("맞은 사람 수"),
                        fieldWithPath("data.correctSubmission").type(JsonFieldType.NUMBER)
                            .description("맞은 제출 수"),
                        fieldWithPath("data.totalSubmission").type(JsonFieldType.NUMBER)
                            .description("총 제출 수"),
                        fieldWithPath("data.answerLength").type(JsonFieldType.NUMBER)
                            .description("정답 글자수 ( 힌트 )"),
                        fieldWithPath("data.consistOf").type(JsonFieldType.STRING)
                            .description("정답 언어 ( 영어면 ENGLISH, 한국어면 KOREAN, 숫자면 NUMERIC )"),
                        fieldWithPath("data.isSolved").type(JsonFieldType.BOOLEAN)
                            .description("푼 문제 여부"),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("문제 배점"),
                    )
                )
            )
    }

    @Test
    @Order(2)
    fun `문제 이의 제기`() {
        // given
        val shortProblem = ShortProblem(
            title = "test11",
            description = "test",
            creator = adminUser!!,
            answer = "test",
            score = 5.0,
        )

        problemRepository.save(shortProblem)

        val accessToken = tokenProvider.createAuthToken(
            email = "test50@test.com",
            expiry = Date(Date().time + 600000),
            role = Role.ROLE_USER.code,
        )

        val urlString = "$PROBLEM_ENDPOINT/{problem_id}/challenge"

        val challengeCreateRequestDto = ChallengeCreateRequest("이것은 이의제기를 위한 내용입니다.")
        val challengeCreateRequestDtoString = objectMapper.writeValueAsString(challengeCreateRequestDto)

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.post(urlString, shortProblem.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(challengeCreateRequestDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .accept(MediaType.APPLICATION_JSON),
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                document(
                    "problems/v2/challenge",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("problem_id").description("문제 id"),
                    ),
                    requestFields(
                        fieldWithPath("content").type(JsonFieldType.STRING)
                            .description("이의제기 내용 ( 최소 10자 ~ 최대 150자 )"),
                    ),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰")
                            .optional(),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data").type(JsonFieldType.BOOLEAN).description("이의제기 데이터 생성 여부"),
                    ),
                ),
            )
    }
}
