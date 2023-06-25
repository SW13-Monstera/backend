package io.csbroker.apiserver.controller.v1.problem

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jayway.jsonpath.JsonPath
import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.controller.IntegrationTest
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemAnswerDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemGradingHistoryDto
import io.csbroker.apiserver.model.ShortProblem
import io.csbroker.apiserver.model.User
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ShortProblemIntegrationTest : IntegrationTest() {
    private val baseUrl = "/api/v1/problems/short"
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
    fun `단답형 문제 조회`() {
        // given
        val problem = save(
            ShortProblem(
                title = "title",
                description = "description",
                creator = user,
                score = 10.0,
                answer = "answer",
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
                val problemId = JsonPath.read(it.response.contentAsString, "$.data.id") as Int
                title shouldBe problem.title
                description shouldBe problem.description
                problemId shouldBe problem.id
            }
    }

    @Test
    fun `단답형 문제 제출 (정답)`() {
        // given
        val problem = save(
            ShortProblem(
                title = "title",
                description = "description",
                creator = user,
                score = 10.0,
                answer = "answer",
            ),
        )

        val answer = "answer"
        val response = request(
            method = HttpMethod.POST,
            url = "$baseUrl/${problem.id}/grade",
            body = ShortProblemAnswerDto(answer = answer),
        )

        response.andExpect(status().isOk)
            .andDo {
                val data = objectMapper.readTree(it.response.contentAsString)
                    .get("data")
                    .toString()
                val responseDto = objectMapper.readValue<ShortProblemGradingHistoryDto>(data)
                print(data)
                responseDto.problemId shouldBe problem.id
                responseDto.title shouldBe problem.title
                responseDto.description shouldBe problem.description
                responseDto.correctSubmission shouldBe 1L
                responseDto.correctUserCnt shouldBe 1L
                responseDto.totalSubmission shouldBe 1L
                responseDto.userAnswer shouldBe answer
                responseDto.answerLength shouldBe problem.answer.length
                responseDto.isAnswer shouldBe true
                responseDto.score shouldBe problem.score
                responseDto.correctAnswer shouldBe problem.answer
            }
    }

    @Test
    fun `단답형 문제 제출 (오답)`() {
        // given
        val problem = save(
            ShortProblem(
                title = "title",
                description = "description",
                creator = user,
                score = 10.0,
                answer = "answer",
            ),
        )

        val answer = "notAnswer"
        val response = request(
            method = HttpMethod.POST,
            url = "$baseUrl/${problem.id}/grade",
            body = ShortProblemAnswerDto(answer = answer),
        )

        response.andExpect(status().isOk)
            .andDo {
                val data = objectMapper.readTree(it.response.contentAsString)
                    .get("data")
                    .toString()
                val responseDto = objectMapper.readValue<ShortProblemGradingHistoryDto>(data)
                print(data)
                responseDto.problemId shouldBe problem.id
                responseDto.title shouldBe problem.title
                responseDto.description shouldBe problem.description
                responseDto.correctSubmission shouldBe 0L
                responseDto.correctUserCnt shouldBe 0L
                responseDto.totalSubmission shouldBe 1L
                responseDto.userAnswer shouldBe answer
                responseDto.answerLength shouldBe problem.answer.length
                responseDto.isAnswer shouldBe false
                responseDto.score shouldBe 0.0
                responseDto.correctAnswer shouldBe problem.answer
            }
    }
}
