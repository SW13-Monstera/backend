package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.controller.IntegrationTest
import io.csbroker.apiserver.dto.problem.ProblemDeleteRequestDto
import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.Problem
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminProblemControllerIntegrationTest : IntegrationTest() {
    @Test
    fun `문제 단일 삭제`() {
        // given
        val problem = save(
            LongProblem(
                title = "문제 제목",
                description = "문제 설명",
                creator = adminUser,
            ),
        )

        // when
        val response = request(
            method = HttpMethod.DELETE,
            url = "/api/admin/problems/${problem.id}",
            isAdmin = true,
        )

        // then
        response.andExpect(status().isOk)
            .andExpect {
                val problems = findAll<Problem>("SELECT p FROM Problem p where p.id = :id", mapOf("id" to problem.id!!))
                problems.size shouldBe 0
            }
    }

    @Test
    fun `문제 여러개 삭제`() {
        // given
        val problem1 = save(
            LongProblem(
                title = "문제 제목",
                description = "문제 설명",
                creator = adminUser,
            ),
        )
        val problem2 = save(
            LongProblem(
                title = "문제 제목",
                description = "문제 설명",
                creator = adminUser,
            ),
        )

        // when
        val response = request(
            method = HttpMethod.DELETE,
            url = "/api/admin/problems",
            isAdmin = true,
            body = ProblemDeleteRequestDto(
                ids = listOf(problem1.id!!, problem2.id!!),
            ),
        )

        // then
        response.andExpect(status().isOk)
            .andExpect {
                val problems = findAll<Problem>(
                    "SELECT p FROM Problem p where p.id in :ids",
                    mapOf("ids" to listOf(problem1.id!!, problem2.id!!)),
                )
                problems.size shouldBe 0
            }
    }
}
