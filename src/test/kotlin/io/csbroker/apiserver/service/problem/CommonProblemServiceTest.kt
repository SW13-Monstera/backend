package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.challenge.CreateChallengeDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.problem.ChallengeRepository
import io.csbroker.apiserver.repository.problem.GradingHistoryRepository
import io.csbroker.apiserver.repository.problem.GradingResultAssessmentRepository
import io.csbroker.apiserver.repository.problem.ProblemRepository
import io.csbroker.apiserver.repository.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class CommonProblemServiceTest {

    private val problemRepository = mockk<ProblemRepository>()
    private val gradingHistoryRepository = mockk<GradingHistoryRepository>()
    private val gradingResultAssessmentRepository = mockk<GradingResultAssessmentRepository>()
    private val userRepository = mockk<UserRepository>()
    private val challengeRepository = mockk<ChallengeRepository>()
    private val user = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )
    private lateinit var commonProblemService: CommonProblemServiceImpl

    @BeforeEach
    fun setUp() {
        commonProblemService = CommonProblemServiceImpl(
            problemRepository,
            gradingHistoryRepository,
            gradingResultAssessmentRepository,
            userRepository,
            challengeRepository,
        )
    }

    @Test
    fun `createChallenge - 존재하지 않는 유저가 이의제기를 할 시 예외가 발생합니다 `() {
        // Arrange
        every { userRepository.findByEmail(any()) } returns null

        // Act & Assert
        assertThrows<EntityNotFoundException> {
            commonProblemService.createChallenge(
                CreateChallengeDto(user, problemId = 1L, content = "Test"),
            )
        }
    }

    @Test
    fun `createChallenge should throw EntityNotFoundException when problem not found`() {
        // Arrange
        // Define necessary mock for user here
        every { userRepository.findByEmail(any()) } returns user

        every { problemRepository.findByIdOrNull(any()) } returns null

        // Act & Assert
        assertThrows<EntityNotFoundException> {
            commonProblemService.createChallenge(
                CreateChallengeDto(user, problemId = 1L, content = "Test"),
            )
        }
    }
}
