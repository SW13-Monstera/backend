package io.csbroker.apiserver.controller.v1.problem

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jayway.jsonpath.JsonPath
import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.controller.IntegrationTest
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemAnswerDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemGradingHistoryDto
import io.csbroker.apiserver.model.Choice
import io.csbroker.apiserver.model.MultipleChoiceProblem
import io.csbroker.apiserver.model.User
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class MultipleProblemIntegrationTest : IntegrationTest() {

    private val baseUrl = "/api/v1/problems/multiple"
    private val objectMapper = jacksonObjectMapper()
    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = save(
            User(
                email = "test-noti@test.com",
                username = "test-noti",
                password = "test-noti",
                providerType = ProviderType.LOCAL,
            ),
        )
    }

    @Test
    fun `객관식 문제 조회`() {
        // given
        val problem = save(
            MultipleChoiceProblem(
                title = "title",
                description = "description",
                creator = user,
                score = 10.0,
                isMultiple = false,
            ),
        )

        val response = request(
            method = HttpMethod.GET,
            url = "$baseUrl/${problem.id}",
        )

        response.andExpect(status().isOk)
            .andDo {
                val title = JsonPath.read(it.response.contentAsString, "$.data.title") as String
                val description = JsonPath.read(it.response.contentAsString, "$.data.description") as String
                title shouldBe problem.title
                description shouldBe problem.description
            }
    }

    @Test
    fun `객관식 문제 제출`() {
        // given
        val problem = save(
            MultipleChoiceProblem(
                title = "title",
                description = "description",
                creator = user,
                score = 10.0,
                isMultiple = false,
            ),
        )

        val choices = listOf(
            Choice(content = "content1", isAnswer = true, multipleChoiceProblem = problem),
            Choice(content = "content2", isAnswer = false, multipleChoiceProblem = problem),
            Choice(content = "content3", isAnswer = false, multipleChoiceProblem = problem),
            Choice(content = "content4", isAnswer = false, multipleChoiceProblem = problem),
            Choice(content = "content5", isAnswer = false, multipleChoiceProblem = problem),
        ).map { save(it) }
        val answer = choices.find { it.isAnswer }
        val requestDto = MultipleChoiceProblemAnswerDto(answerIds = listOf(answer?.id!!))

        val response = request(
            method = HttpMethod.POST,
            url = "$baseUrl/${problem.id}/grade",
            body = requestDto,
        )

        response.andExpect(status().isOk)
            .andDo {
                val jsonNode = objectMapper.readTree(it.response.contentAsString)
                val dataNode = jsonNode.get("data").toString()
                val responseDto = objectMapper.readValue<MultipleChoiceProblemGradingHistoryDto>(dataNode)
                responseDto.problemId shouldBe problem.id
                responseDto.title shouldBe problem.title
                responseDto.description shouldBe problem.description
                responseDto.correctSubmission shouldBe 1L
                responseDto.correctUserCnt shouldBe 1L
                responseDto.totalSubmission shouldBe 1L
                responseDto.choices.map { choice -> choice.id }.containsAll(choices.map { choice -> choice.id })
                responseDto.userAnswerIds.size shouldBe 1
                responseDto.isAnswer shouldBe true
                responseDto.score shouldBe problem.score
            }
    }
}
