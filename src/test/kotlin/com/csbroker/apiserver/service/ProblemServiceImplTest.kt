package com.csbroker.apiserver.service

import com.csbroker.apiserver.common.auth.ProviderType
import com.csbroker.apiserver.dto.ProblemSearchDto
import com.csbroker.apiserver.model.GradingHistory
import com.csbroker.apiserver.model.Problem
import com.csbroker.apiserver.model.ProblemTag
import com.csbroker.apiserver.model.Tag
import com.csbroker.apiserver.model.User
import com.csbroker.apiserver.repository.GradingHistoryRepository
import com.csbroker.apiserver.repository.ProblemRepository
import com.csbroker.apiserver.repository.ProblemTagRepository
import com.csbroker.apiserver.repository.TagRepository
import com.csbroker.apiserver.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("dev")
class ProblemServiceImplTest {
    @Autowired
    private lateinit var problemService: ProblemService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var problemRepository: ProblemRepository

    @Autowired
    private lateinit var tagRepository: TagRepository

    @Autowired
    private lateinit var problemTagRepository: ProblemTagRepository

    @Autowired
    private lateinit var gradingHistoryRepository: GradingHistoryRepository

    @Test
    fun `쿼리_동작_테스트`() {
        // given
        val user = User(
            email = "test@test.com",
            username = "kim",
            password = "",
            providerType = ProviderType.GITHUB
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
            val problem = Problem(
                title = "test$i",
                description = "test",
                answer = "test",
                creator = user
            )

            problemRepository.save(problem)

            if (i <= 2) {
                val gradingHistory = GradingHistory(
                    problem = problem,
                    user = user,
                    userAnswer = "test",
                    score = 9.5f
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

        // when
        val problemSearchDto = ProblemSearchDto(
            tags = listOf("os", "ds"),
            query = "test",
            solvedBy = "kim"
        )
        val pageable = PageRequest.of(0, 10)
        val test = this.problemRepository.findProblemsByQuery(problemSearchDto, pageable)

        // then
        assertThat(test.size).isEqualTo(2)
    }
}
