package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.useranswer.UserAnswerUpsertDto
import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.model.UserAnswer
import io.csbroker.apiserver.repository.problem.GradingStandardRepository
import io.csbroker.apiserver.repository.problem.LongProblemRepository
import io.csbroker.apiserver.repository.problem.UserAnswerGradingStandardRepository
import io.csbroker.apiserver.repository.problem.UserAnswerRepository
import io.csbroker.apiserver.repository.user.UserRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class UserAnswerServiceTest {
    private val userAnswerRepository = mockk<UserAnswerRepository>()
    private val userRepository = mockk<UserRepository>()
    private val longProblemRepository = mockk<LongProblemRepository>()
    private val gradingStandardRepository = mockk<GradingStandardRepository>()
    private val userAnswerGradingStandardRepository = mockk<UserAnswerGradingStandardRepository>()
    private lateinit var service: UserAnswerService
    private val assignee = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )
    private val validator = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )
    private lateinit var userAnswer: UserAnswer
    private lateinit var problem: LongProblem

    @BeforeEach
    fun setUp() {
        service = UserAnswerServiceImpl(
            userAnswerRepository,
            userRepository,
            longProblemRepository,
            gradingStandardRepository,
            userAnswerGradingStandardRepository,
        )
        problem = LongProblem(
            creator = assignee,
            title = "title",
            description = "description",
        )
        problem.id = 1L
        userAnswer = UserAnswer(
            id = 1L,
            answer = "answer",
            problem = problem,
            assignedUser = assignee,
            validatingUser = validator,
        )
    }

    @Test
    fun `createUserAnswer - 존재하지 않는 문제의 답안을 생성시에 예외가 발생합니다`() {
        // given
        val problemId = 1L
        val requestDto = UserAnswerUpsertDto(null, null, "answer", 1L)
        every { longProblemRepository.findByIdOrNull(problemId) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.createUserAnswer(requestDto) }
        verify { longProblemRepository.findByIdOrNull(problemId) }
    }

    @Test
    fun `createUserAnswer - 존재하지 않는 유저가 담당자로 등록되면 예외가 발생합니다 `() {
        // given
        val problemId = problem.id!!
        val notExistUserId = UUID.randomUUID()
        val requestDto = UserAnswerUpsertDto(
            assignedUserId = notExistUserId,
            validatingUserId = validator.id!!,
            answer = "answer",
            problemId = problemId,
        )
        every { longProblemRepository.findByIdOrNull(problemId) } returns problem
        every { userRepository.findByIdOrNull(notExistUserId) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.createUserAnswer(requestDto) }
        verify { longProblemRepository.findByIdOrNull(problemId) }
        verify { userRepository.findByIdOrNull(notExistUserId) }
    }

    @Test
    fun `createUserAnswer - 존재하지 않는 유저가 검수자로 등록되면 예외가 발생합니다 `() {
        // given
        val problemId = problem.id!!
        val notExistUserId = UUID.randomUUID()
        val requestDto = UserAnswerUpsertDto(
            assignedUserId = assignee.id!!,
            validatingUserId = notExistUserId,
            answer = "answer",
            problemId = problemId,
        )
        every { longProblemRepository.findByIdOrNull(problemId) } returns problem
        every { userRepository.findByIdOrNull(assignee.id!!) } returns assignee
        every { userRepository.findByIdOrNull(notExistUserId) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.createUserAnswer(requestDto) }
        verify { longProblemRepository.findByIdOrNull(problemId) }
        verify { userRepository.findByIdOrNull(assignee.id!!) }
        verify { userRepository.findByIdOrNull(notExistUserId) }
    }

    @Test
    fun `createUserAnswer - success`() {
        // given
        val problemId = problem.id!!
        val answer = "answer"
        val requestDto = UserAnswerUpsertDto(
            assignedUserId = assignee.id!!,
            validatingUserId = validator.id!!,
            answer = answer,
            problemId = problemId,
        )
        val userAnswer = UserAnswer(
            id = 1L,
            answer = answer,
            problem = problem,
            assignedUser = assignee,
            validatingUser = validator,
        )

        every { longProblemRepository.findByIdOrNull(problemId) } returns problem
        every { userRepository.findByIdOrNull(assignee.id!!) } returns assignee
        every { userRepository.findByIdOrNull(validator.id!!) } returns validator
        every { userAnswerRepository.save(any()) } returns userAnswer

        // when & then
        val result = service.createUserAnswer(requestDto)
        assertEquals(userAnswer.id, result)
        verify { longProblemRepository.findByIdOrNull(problemId) }
        verify { userRepository.findByIdOrNull(assignee.id!!) }
        verify { userRepository.findByIdOrNull(validator.id!!) }
        verify { userAnswerRepository.save(any()) }
    }

    @Test
    fun `findUserAnswerById - 없는 유저 답안을 조회할 시 예외가 발생합니다`() {
        // given
        every { userAnswerRepository.findByIdOrNull(1L) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.findUserAnswerById(1L) }
        verify { userAnswerRepository.findByIdOrNull(1L) }
    }

    @Test
    fun `labelUserAnswer - 없는 유저 답안에 라벨링을 진행할 시 예외가 발생합니다`() {
        // given
        val email = assignee.email
        val userAnswerId = 1L
        val selectedGradingStandardIds = listOf(1L, 2L, 3L)
        every { userAnswerRepository.findByIdOrNull(userAnswerId) } returns null

        // when & then
        assertThrows<EntityNotFoundException> {
            service.labelUserAnswer(email, userAnswerId, selectedGradingStandardIds)
        }
        verify { userAnswerRepository.findByIdOrNull(userAnswerId) }
    }

    @Test
    fun `labelUserAnswer - 담당자가 존재하지 않는 답안은 라벨링을 할 수 없습니다`() {
        // given
        val anotherUserEmail = "another@email.com"
        val userAnswerId = userAnswer.id!!
        userAnswer.assignedUser = null
        val selectedGradingStandardIds = listOf(1L, 2L, 3L)
        every { userAnswerRepository.findByIdOrNull(userAnswerId) } returns userAnswer

        // when & then
        assertThrows<EntityNotFoundException> {
            service.labelUserAnswer(anotherUserEmail, userAnswerId, selectedGradingStandardIds)
        }
        verify { userAnswerRepository.findByIdOrNull(userAnswerId) }
    }

    @Test
    fun `labelUserAnswer - 담당자가 아닌 유저가 라벨링을 진행할 시 예외가 발생합니다`() {
        // given
        val anotherUserEmail = "another@email.com"
        val userAnswerId = userAnswer.id!!
        val selectedGradingStandardIds = listOf(1L, 2L, 3L)
        every { userAnswerRepository.findByIdOrNull(userAnswerId) } returns userAnswer

        // when & then
        assertThrows<EntityNotFoundException> {
            service.labelUserAnswer(anotherUserEmail, userAnswerId, selectedGradingStandardIds)
        }
        verify { userAnswerRepository.findByIdOrNull(1L) }
    }

    @Test
    fun `labelUserAnswer - 존재하지 않는 채점기준이 존재하면 예외가 발생합니다`() {
        // given
        val userAnswerId = userAnswer.id!!
        val selectedGradingStandardIds = listOf(1L, 2L, 3L)

        every { userAnswerRepository.findByIdOrNull(userAnswerId) } returns userAnswer
        every { gradingStandardRepository.countByIdIn(selectedGradingStandardIds) } returns 0

        // when & then
        assertThrows<EntityNotFoundException> {
            service.labelUserAnswer(assignee.email, userAnswerId, selectedGradingStandardIds)
        }
        verify { gradingStandardRepository.countByIdIn(selectedGradingStandardIds) }
        verify { userAnswerRepository.findByIdOrNull(1L) }
    }

    @Test
    fun `labelUserAnswer - 라벨링이 완료되면 완료 표식을 남깁니다`() {
        // given
        val userAnswerId = userAnswer.id!!
        val selectedGradingStandardIds = listOf(1L, 2L, 3L)

        every { userAnswerRepository.findByIdOrNull(userAnswerId) } returns userAnswer
        every { gradingStandardRepository.countByIdIn(selectedGradingStandardIds) } returns 3
        every { userAnswerGradingStandardRepository.deleteAllByUserAnswerId(userAnswerId) } just runs
        every { userAnswerGradingStandardRepository.batchInsert(userAnswerId, selectedGradingStandardIds) } just runs

        // when & then
        assertEquals(false, userAnswer.isLabeled)
        val result = service.labelUserAnswer(assignee.email, userAnswerId, selectedGradingStandardIds)
        assertEquals(userAnswer.id, result)
        assertEquals(true, userAnswer.isLabeled)
        verify { gradingStandardRepository.countByIdIn(selectedGradingStandardIds) }
        verify { userAnswerRepository.findByIdOrNull(1L) }
        verify { userAnswerGradingStandardRepository.deleteAllByUserAnswerId(userAnswerId) }
        verify { userAnswerGradingStandardRepository.batchInsert(userAnswerId, selectedGradingStandardIds) }
    }

    // Todo : validateUserAnswer 테스트코드 추가
}
