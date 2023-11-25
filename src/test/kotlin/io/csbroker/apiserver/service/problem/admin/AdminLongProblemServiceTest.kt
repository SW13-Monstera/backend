package io.csbroker.apiserver.service.problem.admin

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.problem.GradingStandardRepository
import io.csbroker.apiserver.repository.problem.LongProblemRepository
import io.csbroker.apiserver.repository.problem.ProblemRepository
import io.csbroker.apiserver.repository.problem.StandardAnswerRepository
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

class AdminLongProblemServiceTest {

    private lateinit var longProblemRepository: LongProblemRepository
    private lateinit var problemRepository: ProblemRepository
    private lateinit var userRepository: UserRepository
    private lateinit var gradingStandardRepository: GradingStandardRepository
    private lateinit var tagUpserter: TagUpserter
    private lateinit var standardAnswerRepository: StandardAnswerRepository
    private lateinit var adminLongProblemService: AdminLongProblemService
    private val user = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )

    @BeforeEach
    fun setUp() {
        longProblemRepository = mockk()
        problemRepository = mockk()
        userRepository = mockk()
        gradingStandardRepository = mockk()
        tagUpserter = mockk()
        standardAnswerRepository = mockk()
        adminLongProblemService = AdminLongProblemServiceImpl(
            longProblemRepository,
            problemRepository,
            userRepository,
            gradingStandardRepository,
            tagUpserter,
            standardAnswerRepository,
        )
    }

    @Test
    fun `없는 문제를 검색시 예외 발생`() {
        every { longProblemRepository.findByIdOrNull(any()) } returns null
        assertThrows<EntityNotFoundException> { adminLongProblemService.findProblemById(1L) }
        verify(exactly = 1) { longProblemRepository.findByIdOrNull(any()) }
    }

    @Test
    fun `문제 ID가 존재하면 문제 정보가 반환된다`() {
        // given
        val longProblem = LongProblem(
            creator = user,
            title = "test problem",
            description = "test description",
        )
        longProblem.id = 1L
        every { longProblemRepository.findByIdOrNull(any()) } returns longProblem

        // when
        val result = adminLongProblemService.findProblemById(1L)

        // then
        assertEquals(longProblem.toLongProblemResponseDto(), result)
    }

    @Test
    fun `문제를 생성할 수 있다`() {
        // given
        val createRequestDto = getLongProblemUpsertRequestDto()
        val longProblem = createRequestDto.toLongProblem(user)
        longProblem.id = 1L
        every { problemRepository.save(any()) } returns longProblem
        every { tagUpserter.setTags(any(), any()) } just runs
        every { standardAnswerRepository.saveAll(emptyList()) } returns emptyList()

        // when
        val result = adminLongProblemService.createProblem(createRequestDto, user)

        // then
        assertEquals(longProblem.id, result)
    }

    private fun getLongProblemUpsertRequestDto(): LongProblemUpsertRequestDto {
        return LongProblemUpsertRequestDto(
            title = "Test problem",
            description = "This is a test problem",
            tags = mutableListOf("tag 1", "tag 2"),
            standardAnswers = emptyList(),
            gradingStandards = mutableListOf(),
        )
    }
}
