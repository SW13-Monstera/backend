package io.csbroker.apiserver.controller.v1.admin.problem

import com.jayway.jsonpath.JsonPath
import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.controller.IntegrationTest
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetUpsertRequestDto
import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.MultipleChoiceProblem
import io.csbroker.apiserver.model.ProblemSet
import io.csbroker.apiserver.model.ProblemSetMapping
import io.csbroker.apiserver.model.ShortProblem
import io.csbroker.apiserver.model.User
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AdminProblemSetControllerIntegrationTest : IntegrationTest() {
    @Test
    fun `문제 세트 생성`() {
        // given
        val user = save(
            User(
                email = "problemCreator@csbroker.io",
                username = "problemCreator",
                role = Role.ROLE_ADMIN,
                providerType = ProviderType.LOCAL,
            ),
        )
        val longProblem = save(
            LongProblem(
                title = "long problem",
                description = "long problem description",
                creator = user,
            ),
        )
        val shortProblem = save(
            ShortProblem(
                title = "short problem",
                description = "short problem description",
                creator = user,
                score = 10.0,
                answer = "answer",
            ),
        )

        // when
        val response = request(
            method = HttpMethod.POST,
            url = "/api/admin/problem-sets",
            body = ProblemSetUpsertRequestDto(listOf(longProblem.id, shortProblem.id), "name", "description"),
            isAdmin = true,
        )

        // then
        response.andExpect(MockMvcResultMatchers.status().isOk)
            .andDo {
                val id = JsonPath.read(it.response.contentAsString, "$.data") as Int
                val problemSetMapping = findAll<ProblemSetMapping>(
                    "SELECT psm FROM ProblemSetMapping psm WHERE psm.problemSet.id = :id",
                    mapOf("id" to id.toLong()),
                )
                problemSetMapping.size shouldBe 2
                problemSetMapping.map { psm -> psm.problem.id }.toSet() shouldBe setOf(longProblem.id, shortProblem.id)
            }
    }

    @Test
    fun `문제 세트 수정`() {
        // given
        val user = save(
            User(
                email = "problemCreator2@csbroker.io",
                username = "problemCreator2",
                role = Role.ROLE_ADMIN,
                providerType = ProviderType.LOCAL,
            ),
        )
        val longProblem = save(
            LongProblem(
                title = "long problem",
                description = "long problem description",
                creator = user,
            ),
        )
        val shortProblem = save(
            ShortProblem(
                title = "short problem",
                description = "short problem description",
                creator = user,
                score = 10.0,
                answer = "answer",
            ),
        )
        val multipleChoiceProblem = save(
            MultipleChoiceProblem(
                title = "multiple choice problem",
                description = "multiple choice problem description",
                creator = user,
                score = 10.0,
                isMultiple = false,
            ),
        )
        val problemSet = save(
            ProblemSet(
                name = "set1",
                description = "set1",
            ),
        )
        save(
            ProblemSetMapping(
                problem = longProblem,
                problemSet = problemSet,
            ),
        )
        save(
            ProblemSetMapping(
                problem = shortProblem,
                problemSet = problemSet,
            ),
        )

        // when
        val response = request(
            method = HttpMethod.PUT,
            url = "/api/admin/problem-sets/${problemSet.id}",
            body = ProblemSetUpsertRequestDto(listOf(multipleChoiceProblem.id), "name", "description"),
            isAdmin = true,
        )

        // then
        response.andExpect(MockMvcResultMatchers.status().isOk)
            .andDo {
                val id = JsonPath.read(it.response.contentAsString, "$.data") as Int
                val problemSetMapping = findAll<ProblemSetMapping>(
                    "SELECT psm FROM ProblemSetMapping psm WHERE psm.problemSet.id = :id",
                    mapOf("id" to id.toLong()),
                )
                problemSetMapping.size shouldBe 1
                problemSetMapping.first().problem.id shouldBe multipleChoiceProblem.id
            }
    }
}
