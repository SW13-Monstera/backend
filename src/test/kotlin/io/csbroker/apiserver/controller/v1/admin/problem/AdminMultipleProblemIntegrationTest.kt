package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.controller.IntegrationTest
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import io.csbroker.apiserver.model.MultipleChoiceProblem
import io.csbroker.apiserver.model.ProblemTag
import io.csbroker.apiserver.model.Tag
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AdminMultipleProblemIntegrationTest : IntegrationTest() {
    @Test
    fun `multiple 문제 tag 업데이트`() {
        // given
        val problem = save(
            MultipleChoiceProblem(
                title = "문제 제목",
                description = "문제 설명",
                creator = adminUser,
                score = 10.0,
                isMultiple = false,
            ),
        )
        val tag = save(Tag(name = "${problem.id}-tag1"))
        val newTag = save(Tag(name = "${problem.id}-tag2"))
        save(ProblemTag(problem = problem, tag = tag))

        // when
        val response = request(
            method = HttpMethod.PUT,
            url = "/api/admin/problems/multiple/${problem.id}",
            isAdmin = true,
            body = MultipleChoiceProblemUpsertRequestDto(
                title = problem.title,
                description = problem.description,
                choices = listOf(
                    MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                        content = "choice1",
                        isAnswer = true,
                    ),
                    MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                        content = "choice2",
                        isAnswer = false,
                    ),
                ),
                tags = listOf(newTag.name),
                score = problem.score,
            ),
        )

        // then
        response.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect {
                val problemTags = findAll<ProblemTag>(
                    "SELECT p FROM ProblemTag p join fetch p.tag where p.problem.id = :problemId",
                    mapOf("problemId" to problem.id),
                )
                problemTags.size shouldBe 1
                problemTags[0].tag.name shouldBe newTag.name
            }
    }
}
