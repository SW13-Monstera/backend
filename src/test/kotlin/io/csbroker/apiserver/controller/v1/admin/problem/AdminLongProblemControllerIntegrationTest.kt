package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.controller.IntegrationTest
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.StandardAnswer
import io.csbroker.apiserver.model.Tag
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminLongProblemControllerIntegrationTest : IntegrationTest() {

    @Test
    fun `문제 업데이트`() {
        // given
        val problem = save(
            LongProblem(
                title = "문제 제목",
                description = "문제 설명",
                creator = adminUser,
            ),
        )
        save(StandardAnswer(content = "삭제될 모범 답안", longProblem = problem))
        save(Tag(name="tag1"))
        val updateRequestDto = LongProblemUpsertRequestDto(
            title = "Test problem",
            description = "This is a test problem",
            tags = mutableListOf("tag1"),
            standardAnswers = listOf("업데이트될 모범 답안"),
            gradingStandards = mutableListOf(),
        )

        // when
        val response = request(
            method = HttpMethod.PUT,
            url = "/api/admin/problems/long/${problem.id}",
            isAdmin = true,
            body = updateRequestDto,
        )

        // then
        response.andExpect(status().isOk)
            .andExpect {
                val standardAnswers = findAll<StandardAnswer>("SELECT s FROM StandardAnswer s where s.longProblem.id = :id", mapOf("id" to problem.id))
                standardAnswers.map { it.content }.toSet() shouldBe updateRequestDto.standardAnswers.toSet()
            }

    }
}
