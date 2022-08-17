package com.csbroker.apiserver.e2e

import com.csbroker.apiserver.common.auth.AuthTokenProvider
import com.csbroker.apiserver.common.auth.ProviderType
import com.csbroker.apiserver.common.enums.GradingStandardType
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.dto.problem.GradingResponseDto
import com.csbroker.apiserver.dto.problem.LongProblemAnswerDto
import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemAnswerDto
import com.csbroker.apiserver.dto.problem.ShortProblemAnswerDto
import com.csbroker.apiserver.model.Choice
import com.csbroker.apiserver.model.GradingHistory
import com.csbroker.apiserver.model.GradingStandard
import com.csbroker.apiserver.model.LongProblem
import com.csbroker.apiserver.model.MultipleChoiceProblem
import com.csbroker.apiserver.model.ProblemTag
import com.csbroker.apiserver.model.ShortProblem
import com.csbroker.apiserver.model.Tag
import com.csbroker.apiserver.model.User
import com.csbroker.apiserver.repository.GradingHistoryRepository
import com.csbroker.apiserver.repository.ProblemRepository
import com.csbroker.apiserver.repository.ProblemTagRepository
import com.csbroker.apiserver.repository.TagRepository
import com.csbroker.apiserver.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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

    lateinit var mockWebServer: MockWebServer

    private var longProblemId: Long? = null

    private var multipleChoiceProblemId: Long? = null

    private var shortProblemId: Long? = null

    private var keywordStandardId: Long? = null

    private var choiceId: Long? = null

    private val PROBLEM_ENDPOINT = "/api/v1/problems"

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
                standardAnswer = "test"
            )

            val gradingStandard = GradingStandard(
                content = "test",
                score = 10.0,
                type = GradingStandardType.KEYWORD,
                problem = problem
            )
            problem.gradingStandards.add(gradingStandard)
            problemRepository.save(problem)

            if (i == 1) {
                this.longProblemId = problem.id
                this.keywordStandardId = gradingStandard.id
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

        val shortProblem = ShortProblem(
            title = "test11",
            description = "test",
            creator = user,
            answer = "test",
            score = 5.0
        )

        val multipleProblem = MultipleChoiceProblem(
            title = "test12",
            description = "test",
            creator = user,
            isMultiple = false,
            score = 5.0
        )

        for (i in 1..3) {
            val choice = Choice(
                content = "choice$i",
                isAnswer = i == 3,
                multipleChoiceProblem = multipleProblem
            )
            multipleProblem.addChoice(choice)
        }

        problemRepository.save(shortProblem)
        problemRepository.save(multipleProblem)

        this.shortProblemId = shortProblem.id
        this.multipleChoiceProblemId = multipleProblem.id
        this.choiceId = multipleProblem.choicesList.find { it.content == "choice3" }!!.id!!
        mockWebServer = MockWebServer()
        mockWebServer.start(8081)
    }

    @Test
    @Order(1)
    fun `Long problem 단건 조회`() {
        // given
        val urlString = "$PROBLEM_ENDPOINT/long/{problem_id}"

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(urlString, longProblemId)
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                document(
                    "problems/long/inquire",
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
    fun `Short problem 단건 조회`() {
        // given
        val urlString = "$PROBLEM_ENDPOINT/short/{problem_id}"

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(urlString, shortProblemId)
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                document(
                    "problems/short/inquire",
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
                        fieldWithPath("data.correctCnt").type(JsonFieldType.NUMBER)
                            .description("맞은 사람 수"),
                        fieldWithPath("data.wrongCnt").type(JsonFieldType.NUMBER)
                            .description("틀린 사람 수"),
                        fieldWithPath("data.totalSolved").type(JsonFieldType.NUMBER)
                            .description("문제를 푼 사람 수"),
                        fieldWithPath("data.answerLength").type(JsonFieldType.NUMBER)
                            .description("정답 글자수 ( 힌트 )")
                    )
                )
            )
    }

    @Test
    @Order(3)
    fun `Multiple Choice problem 단건 조회`() {
        // given
        val urlString = "$PROBLEM_ENDPOINT/multiple/{problem_id}"

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(urlString, multipleChoiceProblemId)
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                document(
                    "problems/multiple/inquire",
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
                        fieldWithPath("data.correctCnt").type(JsonFieldType.NUMBER)
                            .description("맞은 사람 수"),
                        fieldWithPath("data.wrongCnt").type(JsonFieldType.NUMBER)
                            .description("틀린 사람 수"),
                        fieldWithPath("data.totalSolved").type(JsonFieldType.NUMBER)
                            .description("문제를 푼 사람 수"),
                        fieldWithPath("data.choices.[].id").type(JsonFieldType.NUMBER)
                            .description("선지 id"),
                        fieldWithPath("data.choices.[].content").type(JsonFieldType.STRING)
                            .description("선지 내용")
                    )
                )
            )
    }

    @Test
    @Order(4)
    fun `problem 검색`() {
        // given
        val query = "test"
        val isSolved = true
        val tags = "os,ds"
        val page = 0
        val size = 10
        val type = "long"
        val isGradable = false

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
                .get(
                    "$PROBLEM_ENDPOINT?query=$query" +
                        "&isSolved=$isSolved&tags=$tags&page=$page&size=$size&type=$type&isGradable=$isGradable"
                )
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
                        parameterWithName("size").description("가져올 문제의 개수"),
                        parameterWithName("type").description("문제의 type ( long, short, multiple )"),
                        parameterWithName("isGradable").description("채점 가능 여부")
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
                            .description("문제를 푼 사람 수"),
                        fieldWithPath("data.[].type").type(JsonFieldType.STRING)
                            .description("문제의 타입 ( short, multiple, choice )")
                    )
                )
            )
    }

    @Test
    @Order(5)
    fun `Long problem 채점`() {
        // given
        val urlString = "$PROBLEM_ENDPOINT/long/{problem_id}/grade"

        val userAnswer = "정답의 키워드는 test를 포함해야합니다."
        val userAnswerDto = LongProblemAnswerDto(userAnswer)
        val userAnswerDtoString = objectMapper.writeValueAsString(userAnswerDto)

        val now = Date()
        val email = "test2@test.com"

        val accessToken = tokenProvider.createAuthToken(
            email = email,
            expiry = Date(now.time + 600000),
            role = Role.ROLE_USER.code
        )

        val mockGradingResponse = GradingResponseDto(
            longProblemId!!,
            listOf(
                GradingResponseDto.CorrectKeyword(
                    keywordStandardId!!,
                    "test",
                    listOf(9, 13),
                    "test"
                )
            )
        )

        val mockGradingResponseString = objectMapper.writeValueAsString(mockGradingResponse)

        mockWebServer.enqueue(
            MockResponse().setBody(mockGradingResponseString)
                .addHeader("Content-Type", "application/json")
        )

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders
                .post(urlString, longProblemId!!)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userAnswerDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                document(
                    "problems/long/grade",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰")
                            .optional()
                    ),
                    pathParameters(
                        parameterWithName("problem_id").description("문제 id")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.gradingHistoryId").type(JsonFieldType.NUMBER).description("채점 결과 id"),
                        fieldWithPath("data.problemId").type(JsonFieldType.NUMBER).description("문제 id"),
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
                            .description("문제를 푼 사람 수"),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("채점 된 유저 답안의 점수"),
                        fieldWithPath("data.userAnswer").type(JsonFieldType.STRING)
                            .description("채점 된 유저 답안"),
                        fieldWithPath("data.standardAnswer").type(JsonFieldType.STRING)
                            .description("문제의 모범 답안"),
                        fieldWithPath("data.keywords").type(JsonFieldType.ARRAY)
                            .description("답안에 들어가야하는 키워드"),
                        fieldWithPath("data.keywords.[].id").type(JsonFieldType.NUMBER)
                            .description("키워드 id"),
                        fieldWithPath("data.keywords.[].content").type(JsonFieldType.STRING)
                            .description("키워드 내용"),
                        fieldWithPath("data.keywords.[].isExist").type(JsonFieldType.BOOLEAN)
                            .description("키워드가 유저답안에 존재하는지 유무"),
                        fieldWithPath("data.keywords.[].idx").type(JsonFieldType.ARRAY)
                            .description("키워드가 유저답안에 존재 할 때, 시작 index와 끝 index ( 존재하지 않으면 빈 배열 )")
                    )
                )
            )
    }

    @Test
    @Order(6)
    fun `Short problem 채점`() {
        // given
        val urlString = "$PROBLEM_ENDPOINT/short/{problem_id}/grade"

        val userAnswer = "test"
        val userAnswerDto = ShortProblemAnswerDto(userAnswer)
        val userAnswerDtoString = objectMapper.writeValueAsString(userAnswerDto)

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
                .post(urlString, shortProblemId!!)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userAnswerDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                document(
                    "problems/short/grade",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰")
                            .optional()
                    ),
                    pathParameters(
                        parameterWithName("problem_id").description("문제 id")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.gradingHistoryId").type(JsonFieldType.NUMBER).description("채점 결과 id"),
                        fieldWithPath("data.problemId").type(JsonFieldType.NUMBER).description("문제 id"),
                        fieldWithPath("data.title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        fieldWithPath("data.tags").type(JsonFieldType.ARRAY).description("태그"),
                        fieldWithPath("data.correctCnt").type(JsonFieldType.NUMBER)
                            .description("맞은 사람 수"),
                        fieldWithPath("data.wrongCnt").type(JsonFieldType.NUMBER)
                            .description("틀린 사람 수"),
                        fieldWithPath("data.totalSolved").type(JsonFieldType.NUMBER)
                            .description("문제를 푼 사람 수"),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("채점 된 유저 답안의 점수"),
                        fieldWithPath("data.userAnswer").type(JsonFieldType.STRING)
                            .description("채점 된 유저 답안"),
                        fieldWithPath("data.answerLength").type(JsonFieldType.NUMBER)
                            .description("모범 답안의 글자 수"),
                        fieldWithPath("data.isAnswer").type(JsonFieldType.BOOLEAN)
                            .description("유저 답안의 정답 여부")
                    )
                )
            )
    }

    @Test
    @Order(7)
    fun `Multiple choice problem 채점`() {
        // given
        val urlString = "$PROBLEM_ENDPOINT/multiple/{problem_id}/grade"

        val answerIds = listOf(this.choiceId!!)
        val userAnswerDto = MultipleChoiceProblemAnswerDto(answerIds)
        val userAnswerDtoString = objectMapper.writeValueAsString(userAnswerDto)

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
                .post(urlString, multipleChoiceProblemId!!)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userAnswerDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                document(
                    "problems/multiple/grade",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰")
                            .optional()
                    ),
                    pathParameters(
                        parameterWithName("problem_id").description("문제 id")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.gradingHistoryId").type(JsonFieldType.NUMBER).description("채점 결과 id"),
                        fieldWithPath("data.problemId").type(JsonFieldType.NUMBER).description("문제 id"),
                        fieldWithPath("data.title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.description").type(JsonFieldType.STRING)
                            .description("문제 설명"),
                        fieldWithPath("data.tags").type(JsonFieldType.ARRAY).description("태그"),
                        fieldWithPath("data.correctCnt").type(JsonFieldType.NUMBER)
                            .description("맞은 사람 수"),
                        fieldWithPath("data.wrongCnt").type(JsonFieldType.NUMBER)
                            .description("틀린 사람 수"),
                        fieldWithPath("data.totalSolved").type(JsonFieldType.NUMBER)
                            .description("문제를 푼 사람 수"),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("채점 된 유저 답안의 점수"),
                        fieldWithPath("data.userAnswerIds").type(JsonFieldType.ARRAY)
                            .description("채점 된 유저 답안 ( 고른 선지의 id 배열 )"),
                        fieldWithPath("data.isAnswer").type(JsonFieldType.BOOLEAN)
                            .description("유저 답안의 정답 여부"),
                        fieldWithPath("data.choices").type(JsonFieldType.ARRAY)
                            .description("문제의 선지"),
                        fieldWithPath("data.choices").type(JsonFieldType.ARRAY)
                            .description("문제의 선지"),
                        fieldWithPath("data.choices").type(JsonFieldType.ARRAY)
                            .description("문제의 선지"),
                        fieldWithPath("data.choices.[].id").type(JsonFieldType.NUMBER)
                            .description("선지 id"),
                        fieldWithPath("data.choices.[].content").type(JsonFieldType.STRING)
                            .description("선지 내용")
                    )
                )
            )
    }
}
