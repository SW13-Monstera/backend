package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.controller.IntegrationTest
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.ProblemTag
import io.csbroker.apiserver.model.StandardAnswer
import io.csbroker.apiserver.model.Tag
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminLongProblemControllerIntegrationTest : IntegrationTest() {

    @Test
    fun `문제 업데이트 - 모범 답안`() {
        // given
        val longProblem = LongProblem(title = "문제 제목", description = "문제 설명", creator = adminUser)
        save(longProblem)
        save(StandardAnswer(content = "삭제될 모범 답안", longProblem = longProblem))
        val updateRequestDto = LongProblemUpsertRequestDto(
            title = "Test problem",
            description = "This is a test problem",
            tags = mutableListOf(),
            standardAnswers = listOf("업데이트될 모범 답안"),
        )

        // when
        val response = request(
            method = HttpMethod.PUT,
            url = "/api/admin/problems/long/${longProblem.id}",
            isAdmin = true,
            body = updateRequestDto,
        )

        // then
        response.andExpect(status().isOk)
            .andExpect {
                val standardAnswers = findAll<StandardAnswer>(
                    "SELECT s FROM StandardAnswer s where s.longProblem.id = :id",
                    mapOf("id" to longProblem.id),
                )
                standardAnswers.map { it.content }.toSet() shouldBe updateRequestDto.standardAnswers.toSet()
                standardAnswers.size shouldBe updateRequestDto.standardAnswers.size
            }
    }

    @Test
    fun `문제 업데이트 - 태그`() {
        // given
        val oldTag = save(Tag(name = "long-problem-update-tag1"))
        val longProblem = LongProblem(title = "문제 제목", description = "문제 설명", creator = adminUser)
        longProblem.addTag(oldTag)
        save(longProblem)
        val newTag1 = save(Tag(name = "${longProblem.id}-tag2"))
        val newTag2 = save(Tag(name = "${longProblem.id}-tag3"))
        val updateRequestDto = LongProblemUpsertRequestDto(
            title = longProblem.title,
            description = longProblem.description,
            tags = mutableListOf(newTag1.name, newTag2.name),
            standardAnswers = emptyList(),
g        )

        // when
        val response = request(
            method = HttpMethod.PUT,
            url = "/api/admin/problems/long/${longProblem.id}",
            isAdmin = true,
            body = updateRequestDto,
        )

        // then
        response.andExpect(status().isOk)
            .andExpect {
                val problemTags = findAll<ProblemTag>(
                    "SELECT pt FROM ProblemTag pt JOIN FETCH pt.tag where pt.problem.id = :id",
                    mapOf("id" to longProblem.id),
                )
                problemTags.map { it.tag.name }.toSet() shouldBe updateRequestDto.tags.toSet()
                problemTags.size shouldBe updateRequestDto.tags.size
            }
    }
}
