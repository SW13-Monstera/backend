package io.csbroker.apiserver.controller.v1.problem

import com.jayway.jsonpath.JsonPath
import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.controller.IntegrationTest
import io.csbroker.apiserver.controller.v2.problem.response.SubmitLongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemAnswerDto
import io.csbroker.apiserver.model.GradingHistory
import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.StandardAnswer
import io.csbroker.apiserver.model.User
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.transaction.annotation.Transactional



class LongProblemIntegrationTest : IntegrationTest() {

    private val baseUrl = "/api/v1/problems/long"
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
            )
        )

    }

    @Transactional
    @Test
    fun `서술형 문제 조회`() {
        // given & when

        val problem = getProblem()
        val response = request(
            method = HttpMethod.GET,
            url = "${baseUrl}/${problem.id}",
        )

        // then
        response.andExpect(status().isOk)
            .andDo {
                val title = JsonPath.read(it.response.contentAsString, "$.data.title") as String
                val description = JsonPath.read(it.response.contentAsString, "$.data.description") as String
                title shouldBe problem.title
                description shouldBe problem.description
            }
    }

    @Transactional
    @Test
    fun `서술형 문제 제출`() {
        val problem = getProblem()

        // given & when
        val preSubmissionCount = findAll<GradingHistory>(
            "SELECT gh From GradingHistory gh where gh.problem.id = :id",
            mapOf("id" to problem.id!!),
        ).size
        println(preSubmissionCount)
        val preUserSubmissionCount = findAll<GradingHistory>(
            "SELECT gh From GradingHistory gh where gh.problem.id = :problemId AND gh.user.id = :userId",
            mapOf("problemId" to problem.id!!, "userId" to user.id!!),
        ).size
        println(preUserSubmissionCount)
        val standardAnswers = findAll<StandardAnswer>(
            "SELECT sa From StandardAnswer sa where sa.longProblem.id = :id",
            mapOf("id" to problem.id!!)
        )
        println(standardAnswers)

        val userAnswer = "answer"
        val response = request(
            method = HttpMethod.POST,
            url = "${baseUrl}/${problem.id}/submit",
            body = LongProblemAnswerDto(userAnswer)
        )

        //userAnswer 생성 확인

        println(response)
        response.andExpect(status().isOk)
            .andDo {
                val jsonNode = objectMapper.readTree(it.response.contentAsString)
                val dataNode = jsonNode.get("data")
                val dataAsString = dataNode.toString()
                val responseDto = objectMapper.readValue<SubmitLongProblemResponseDto>(dataAsString)
                responseDto.title shouldBe problem.title
                responseDto.description shouldBe problem.description
                responseDto.totalSubmissionCount shouldBe preSubmissionCount + 1  // 제출 수가 증가하는지 확인
                responseDto.userSubmissionCount shouldBe preUserSubmissionCount + 1  // 제출 수가 증가하는지 확인
                responseDto.userAnswer shouldBe userAnswer
                standardAnswers.map { sa -> sa.content } shouldContain responseDto.standardAnswer  // 모법답안중 하나가 반환되는지 확인
            }
    }

    fun getProblem(): LongProblem {
        val problem = save(
            LongProblem(
                title = "title",
                description = "description",
                creator = user,
            )
        )
        save(StandardAnswer(content = "standard answer1", longProblem = problem))
        save(StandardAnswer(content = "standard answer2", longProblem = problem))

        return problem
    }

}
