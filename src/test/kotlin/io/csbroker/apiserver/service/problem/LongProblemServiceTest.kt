package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.client.AIServerClient
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.controller.v2.problem.request.SubmitLongProblemDto
import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.StandardAnswer
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.model.UserAnswer
import io.csbroker.apiserver.repository.problem.GradingHistoryRepository
import io.csbroker.apiserver.repository.problem.LongProblemRepository
import io.csbroker.apiserver.repository.problem.StandardAnswerRepository
import io.csbroker.apiserver.repository.problem.UserAnswerRepository
import io.csbroker.apiserver.repository.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class LongProblemServiceTest {

    private val longProblemRepository = mockk<LongProblemRepository>()
    private val userRepository = mockk<UserRepository>()
    private val userAnswerRepository = mockk<UserAnswerRepository>()
    private val standardAnswerRepository = mockk<StandardAnswerRepository>()
    private val gradingHistoryRepository = mockk<GradingHistoryRepository>()
    private val aiServerClient = mockk<AIServerClient>()
    private val user = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )

    private lateinit var longProblemService: LongProblemService

    @BeforeEach
    fun setUp() {
        longProblemService = LongProblemServiceImpl(
            longProblemRepository,
            userRepository,
            userAnswerRepository,
            standardAnswerRepository,
            gradingHistoryRepository,
            aiServerClient,
        )
    }

    @Test
    fun `submitProblem - 존재하지 않는 문제에 대한 답안을 제출할 시 예외가 발생합니다`() {
        // given
        val submitRequest = SubmitLongProblemDto(user = user, problemId = 1L, answer = "test answer")
        every { longProblemRepository.findByIdOrNull(1L) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { longProblemService.submitProblem(submitRequest) }
        verify { longProblemRepository.findByIdOrNull(1L) }
    }

    @Test
    fun `submitProblem - 모범 답안이 존재하지 않으면 예외가 발생합니다`() {
        // given
        val email = "test@test.com"
        val problemId = 1L
        val answer = "test answer"
        val submitRequest = SubmitLongProblemDto(user, problemId, answer)
        val title = "test problem"
        val description = "test description"
        val longProblem = LongProblem(
            creator = user,
            title = title,
            description = description,
        )
        val userAnswer = UserAnswer(answer = answer, problem = longProblem)
        every { longProblemRepository.findByIdOrNull(problemId) } returns longProblem
        every { userAnswerRepository.save(any<UserAnswer>()) } returns userAnswer
        every { standardAnswerRepository.findAllByLongProblem(longProblem) } returns emptyList()

        // when && then
        assertThrows<EntityNotFoundException> { longProblemService.submitProblem(submitRequest) }

        verify { longProblemRepository.findByIdOrNull(problemId) }
        verify { userAnswerRepository.save(any<UserAnswer>()) }
        verify { standardAnswerRepository.findAllByLongProblem(longProblem) }
    }

    @Test
    fun `submitProblem - success`() {
        // given
        val email = "test@test.com"
        val problemId = 1L
        val answer = "test answer"
        val submitRequest = SubmitLongProblemDto(user, problemId, answer)
        val title = "test problem"
        val description = "test description"
        val content = "std content"
        val longProblem = LongProblem(
            creator = user,
            title = title,
            description = description,
        )
        val standardAnswer = StandardAnswer(content = "std content", longProblem = longProblem)
        val userAnswer = UserAnswer(answer = answer, problem = longProblem)
        every { longProblemRepository.findByIdOrNull(problemId) } returns longProblem
        every { userAnswerRepository.save(any<UserAnswer>()) } returns userAnswer
        every { standardAnswerRepository.findAllByLongProblem(longProblem) } returns listOf(standardAnswer)
        every { gradingHistoryRepository.save(any()) } returns mockk()

        // when
        val result = longProblemService.submitProblem(submitRequest)

        // then
        assertEquals(title, result.title)
        assertEquals(description, result.description)
        assertEquals(answer, result.userAnswer)
        assertEquals(content, result.standardAnswer)

        verify { longProblemRepository.findByIdOrNull(problemId) }
        verify { userAnswerRepository.save(any<UserAnswer>()) }
        verify { standardAnswerRepository.findAllByLongProblem(longProblem) }
    }
}
