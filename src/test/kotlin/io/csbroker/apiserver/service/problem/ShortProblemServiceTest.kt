package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.LikeType
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.grade.ShortProblemGradingRequestDto
import io.csbroker.apiserver.model.GradingHistory
import io.csbroker.apiserver.model.ShortProblem
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.post.LikeRepository
import io.csbroker.apiserver.repository.problem.GradingHistoryRepository
import io.csbroker.apiserver.repository.problem.ShortProblemRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class ShortProblemServiceTest {

    private val shortProblemRepository = mockk<ShortProblemRepository>()
    private val likeRepository = mockk<LikeRepository>()
    private val gradingHistoryRepository = mockk<GradingHistoryRepository>()
    private val user = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )
    private lateinit var service: ShortProblemService

    @BeforeEach
    fun setUp() {
        service = ShortProblemServiceImpl(
            shortProblemRepository,
            likeRepository,
            gradingHistoryRepository,
        )
    }

    @Test
    fun `findProblemById - 없는 문제를 조회할 시 예외가 발생합니다`() {
        // given
        every { shortProblemRepository.findByIdOrNull(any()) } returns null
        every { likeRepository.findAllByTargetId(LikeType.PROBLEM, any()) } returns emptyList()

        // when & then
        assertThrows<EntityNotFoundException> { service.findProblemById(1L, user.email) }
        verify { shortProblemRepository.findByIdOrNull(any()) }
    }

    @Test
    fun `gradingProblem - 없는 문제에 답안을 제출할 시 예외가 발생합니다`() {
        // given
        val email = user.email
        val problemId = 1L
        val answer = "answer"
        val gradingRequest = ShortProblemGradingRequestDto(
            user,
            problemId,
            answer,
        )
        every { shortProblemRepository.findByIdOrNull(problemId) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.gradingProblem(gradingRequest) }
        verify { shortProblemRepository.findByIdOrNull(problemId) }
    }

    @Test
    fun `gradingProblem - 정답을 제출할 시 점수가 부여됩니다`() {
        // given
        val problem = createProblem()
        val gradingRequest = ShortProblemGradingRequestDto(
            user,
            problem.id,
            problem.answer,
        )
        val gradingHistory = GradingHistory(
            gradingHistoryId = 1L,
            problem = problem,
            user = user,
            userAnswer = problem.answer,
            score = problem.score,
        )
        every { shortProblemRepository.findByIdOrNull(problem.id) } returns problem
        every { gradingHistoryRepository.save(any()) } returns gradingHistory

        // when
        val result = service.gradingProblem(gradingRequest)

        // then
        assertEquals(problem.score, result.score)
        verify { shortProblemRepository.findByIdOrNull(problem.id) }
        verify { gradingHistoryRepository.save(any()) }
    }

    @Test
    fun `gradingProblem - 오답을 제출할 시 점수가 부여됩니다`() {
        // given
        val problem = createProblem()
        val wrongAnswer = "wrongAnswer"
        val gradingRequest = ShortProblemGradingRequestDto(
            user,
            problem.id,
            wrongAnswer,
        )
        val gradingHistory = GradingHistory(
            gradingHistoryId = 1L,
            problem = problem,
            user = user,
            userAnswer = wrongAnswer,
            score = 10.0,
        )
        every { shortProblemRepository.findByIdOrNull(problem.id) } returns problem
        every { gradingHistoryRepository.save(any()) } returns gradingHistory
        // when
        val result = service.gradingProblem(gradingRequest)

        // then
        assertEquals(0.0, result.score)
        verify { shortProblemRepository.findByIdOrNull(problem.id) }
        verify { gradingHistoryRepository.save(any()) }
    }

    private fun createProblem(): ShortProblem {
        val problem = ShortProblem(
            title = "title",
            description = "description",
            creator = user,
            score = 10.0,
            answer = "answer",
        )
        problem.id = 1L
        return problem
    }
}
