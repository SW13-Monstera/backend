package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.LikeType
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.grade.MultipleProblemGradingRequestDto
import io.csbroker.apiserver.model.Choice
import io.csbroker.apiserver.model.GradingHistory
import io.csbroker.apiserver.model.MultipleChoiceProblem
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.post.LikeRepository
import io.csbroker.apiserver.repository.problem.GradingHistoryRepository
import io.csbroker.apiserver.repository.problem.MultipleChoiceProblemRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class MultipleProblemServiceTest {

    private val multipleChoiceProblemRepository = mockk<MultipleChoiceProblemRepository>()
    private val likeRepository = mockk<LikeRepository>()
    private val gradingHistoryRepository = mockk<GradingHistoryRepository>()
    private val user = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )
    private lateinit var service: MultipleProblemService

    @BeforeEach
    fun setUp() {
        service = MultipleProblemServiceImpl(
            multipleChoiceProblemRepository,
            likeRepository,
            gradingHistoryRepository,
        )
    }

    @Test
    fun `findProblemById - 없는 문제를 찾을 시 예외가 발생합니다`() {
        // given
        every { likeRepository.findAllByTargetId(LikeType.PROBLEM, 1L) } returns emptyList()
        every { multipleChoiceProblemRepository.findByIdOrNull(any()) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.findProblemById(1L, "email") }
        verify { multipleChoiceProblemRepository.findByIdOrNull(any()) }
    }

    @Test
    fun `findProblemById - success`() {
        val problemId = 1L

        val mockProblem = mockk<MultipleChoiceProblem>()
        every { likeRepository.findAllByTargetId(LikeType.PROBLEM, 1) } returns emptyList()
        every { mockProblem.toDetailResponseDto(user.email, emptyList()) } returns mockk(relaxed = true)
        every { multipleChoiceProblemRepository.findByIdOrNull(problemId) } returns mockProblem

        val responseDto = service.findProblemById(problemId, user.email)
        assertEquals(responseDto, mockProblem.toDetailResponseDto(user.email, emptyList()))
    }

    @Test
    fun `gradingProblem - 존재하지 않는 문제에 대한 답안을 제출시 예외가 발생합니다`() {
        // given
        val problemId = 1L
        val answerIds = listOf(1L, 2L)
        val gradingRequest = MultipleProblemGradingRequestDto(user, problemId, answerIds)
        every { multipleChoiceProblemRepository.findByIdOrNull(problemId) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.gradingProblem(gradingRequest) }
        verify { multipleChoiceProblemRepository.findByIdOrNull(problemId) }
    }

    @Test
    fun `gradingProblem - 정답을 맞추면 배점만큼 점수만큼 부여됩니다`() {
        // given
        val email = user.email
        val problem = createProblem()
        val problemId = problem.id
        val answerId = problem.choicesList.first { it.isAnswer }.id
        val answerIds = listOf(answerId)
        val gradingRequest = MultipleProblemGradingRequestDto(user, problemId, answerIds)
        val gradingHistory = GradingHistory(
            gradingHistoryId = 1L,
            problem = problem,
            user = user,
            userAnswer = answerId.toString(),
            score = problem.score,
        )
        every { multipleChoiceProblemRepository.findByIdOrNull(problemId) } returns problem
        every { gradingHistoryRepository.save(any()) } returns gradingHistory

        // when
        val result = service.gradingProblem(gradingRequest)
        assertEquals(problem.score, result.score)
    }

    @Test
    fun `gradingProblem - 오답이면 0점이 부여됩니다`() {
        // given
        val email = user.email
        val problem = createProblem()
        val problemId = problem.id
        val answerId = problem.choicesList.first { !it.isAnswer }.id
        val answerIds = listOf(answerId)
        val gradingRequest = MultipleProblemGradingRequestDto(user, problemId, answerIds)
        val gradingHistory = GradingHistory(
            gradingHistoryId = 1L,
            problem = problem,
            user = user,
            userAnswer = answerId.toString(),
            score = 0.0,
        )
        every { multipleChoiceProblemRepository.findByIdOrNull(problemId) } returns problem
        every { gradingHistoryRepository.save(any()) } returns gradingHistory

        // when
        val result = service.gradingProblem(gradingRequest)
        assertEquals(0.0, result.score)
    }

    private fun createProblem(): MultipleChoiceProblem {
        val problem = MultipleChoiceProblem(
            title = "title",
            description = "description",
            creator = user,
            score = 10.0,
            isMultiple = true,
        )
        val choiceList = listOf(
            Choice(
                id = 1L,
                content = "correct answer",
                isAnswer = true,
                multipleChoiceProblem = problem,
            ),
            Choice(
                id = 2L,
                content = "wrong answer1",
                isAnswer = false,
                multipleChoiceProblem = problem,
            ),
            Choice(
                id = 1L,
                content = "wrong answer2",
                isAnswer = false,
                multipleChoiceProblem = problem,
            ),
        )
        choiceList.forEach { problem.choicesList.add(it) }
        problem.id = 1L
        return problem
    }
}
