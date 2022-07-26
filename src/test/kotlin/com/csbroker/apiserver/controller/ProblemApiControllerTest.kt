package com.csbroker.apiserver.controller

import com.csbroker.apiserver.common.auth.AuthTokenProvider
import com.csbroker.apiserver.common.auth.ProviderType
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.model.GradingHistory
import com.csbroker.apiserver.model.LongProblem
import com.csbroker.apiserver.model.ProblemTag
import com.csbroker.apiserver.model.Tag
import com.csbroker.apiserver.model.User
import com.csbroker.apiserver.repository.GradingHistoryRepository
import com.csbroker.apiserver.repository.ProblemRepository
import com.csbroker.apiserver.repository.ProblemTagRepository
import com.csbroker.apiserver.repository.TagRepository
import com.csbroker.apiserver.repository.UserRepository
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
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.Date

@SpringBootTest
@AutoConfigureRestDocs
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProblemApiControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var problemRepository: ProblemRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var tagRepository: TagRepository

    @Autowired
    private lateinit var problemTagRepository: ProblemTagRepository

    @Autowired
    private lateinit var gradingHistoryRepository: GradingHistoryRepository

    @Autowired
    private lateinit var tokenProvider: AuthTokenProvider

    private var problemId: Long? = null

    private val PROBLEM_ENDPOINT = "/api/problems"

    @BeforeAll
    fun setUpData() {
        val user = User(
            email = "test2@test.com",
            username = "test2",
            providerType = ProviderType.LOCAL
        )

        userRepository.save(user)

        val osTag = Tag(
            name = "os"
        )
        tagRepository.save(osTag)

        val dsTag = Tag(
            name = "ds"
        )
        tagRepository.save(dsTag)

        for (i in 1..10) {
            val problem = LongProblem(
                title = "test$i",
                description = "test",
                creator = user,
                standardAnswer = "test",
                isGradable = true
            )

            problemRepository.save(problem)

            if (i == 1) {
                this.problemId = problem.id
            }

            if (i <= 2) {
                val gradingHistory = GradingHistory(
                    problem = problem,
                    user = user,
                    userAnswer = "test",
                    score = 9.5
                )
                gradingHistoryRepository.save(gradingHistory)
            }

            if (i <= 5) {
                val problemTagOS = ProblemTag(
                    problem = problem,
                    tag = osTag
                )

                problemTagRepository.save(problemTagOS)
            } else {
                val problemTagDs = ProblemTag(
                    problem = problem,
                    tag = dsTag
                )
                problemTagRepository.save(problemTagDs)
            }
        }
    }

    @Test
    @Order(1)
    fun `problem 단건 조회`() {
        // given
        val urlString = "$PROBLEM_ENDPOINT/{problem_id}"

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(urlString, problemId)
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                document(
                    "problems/inquire",
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
                        fieldWithPath("data.avgScore").type(JsonFieldType.NUMBER)
                            .description("평균 점수 ( 푼 사람이 없는 경우 null return )").optional(),
                        fieldWithPath("data.topScore").type(JsonFieldType.NUMBER)
                            .description("최고 점수 ( 푼 사람이 없는 경우 null return )").optional(),
                        fieldWithPath("data.bottomScore").type(JsonFieldType.NUMBER)
                            .description("최저 점수 ( 푼 사람이 없는 경우 null return )").optional(),
                        fieldWithPath("data.totalSolved").type(JsonFieldType.NUMBER)
                            .description("문제를 푼 사람 수")
                    )
                )
            )
    }

    @Test
    @Order(2)
    fun `problem 검색`() {
        // given
        val query = "test"
        val isSolved = true
        val tags = "os,ds"
        val page = 0
        val size = 10

        val now = Date()
        val email = "test2@test.com"

        val accessToken = tokenProvider.createAuthToken(
            email = email,
            expiry = Date(now.time + 600000),
            role = Role.ROLE_USER.code
        )

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders
                .get("$PROBLEM_ENDPOINT?query=$query&isSolved=$isSolved&tags=$tags&page=$page&size=$size")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                document(
                    "problems/search",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰 ( 푼 문제로 검색을 하는 경우가 아니라면, 포함하지 않아도 됨. )")
                            .optional()
                    ),
                    requestParameters(
                        parameterWithName("query").description("검색어"),
                        parameterWithName("isSolved").description("풀이 여부"),
                        parameterWithName("tags").description("문제의 태그들"),
                        parameterWithName("page").description("페이지"),
                        parameterWithName("size").description("가져올 문제의 개수")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.[].id").type(JsonFieldType.NUMBER).description("문제 id"),
                        fieldWithPath("data.[].title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.[].tags").type(JsonFieldType.ARRAY).description("태그"),
                        fieldWithPath("data.[].avgScore").type(JsonFieldType.NUMBER)
                            .description("평균 점수 ( 푼 사람이 없는 경우 null return )").optional(),
                        fieldWithPath("data.[].totalSolved").type(JsonFieldType.NUMBER)
                            .description("문제를 푼 사람 수")
                    )
                )
            )
    }
}
