package io.csbroker.apiserver.service.problem.admin

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto.ChoiceData
import io.csbroker.apiserver.model.MultipleChoiceProblem
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.problem.ChoiceRepository
import io.csbroker.apiserver.repository.problem.MultipleChoiceProblemRepository
import io.csbroker.apiserver.repository.problem.ProblemRepository
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class AdminMultipleProblemServiceTest {
    private lateinit var multipleChoiceProblemRepository: MultipleChoiceProblemRepository
    private lateinit var choiceRepository: ChoiceRepository
    private lateinit var problemRepository: ProblemRepository
    private lateinit var userRepository: UserRepository
    private lateinit var tagUpserter: TagUpserter
    private lateinit var adminMultipleProblemService: AdminMultipleProblemService
    private lateinit var user: User
    private lateinit var problem: MultipleChoiceProblem

    @BeforeEach
    fun setUp() {
        multipleChoiceProblemRepository = mockk()
        choiceRepository = mockk()
        problemRepository = mockk()
        userRepository = mockk()
        tagUpserter = mockk()
        adminMultipleProblemService = AdminMultipleProblemServiceImpl(
            multipleChoiceProblemRepository,
            choiceRepository,
            problemRepository,
            userRepository,
            tagUpserter,
        )
        user = User(
            id = UUID.randomUUID(),
            email = "test@test.com",
            password = "test1234!",
            username = "test",
            providerType = ProviderType.LOCAL,
        )
        problem = MultipleChoiceProblem(
            title = "title",
            description = "description",
            creator = user,
            score = 10.0,
            isMultiple = true,
        )
        problem.id = 1L
    }

    @Test
    fun `findProblems - 검색 조건이 없을 때`() {
        // given
        val pageable = PageRequest.of(0, 10)
        val pagedProblems = PageImpl<MultipleChoiceProblem>(emptyList())

        val adminProblemSearchDto = AdminProblemSearchDto(null, null, null, pageable)

        every {
            multipleChoiceProblemRepository.findMultipleChoiceProblemsByQuery(null, null, null, pageable)
        } returns pagedProblems

        // when
        val result: MultipleChoiceProblemSearchResponseDto = adminMultipleProblemService.findProblems(
            adminProblemSearchDto,
        )

        // then
        verify { multipleChoiceProblemRepository.findMultipleChoiceProblemsByQuery(null, null, null, pageable) }
        assertEquals(pagedProblems.totalElements, result.totalElements)
        assertEquals(pagedProblems.totalPages, result.totalPages)
    }

    @Test
    fun `findProblems - 검색 조건이 있을 때`() {
        // given
        val pageable = PageRequest.of(0, 10)

        val pagedProblems = PageImpl(listOf(problem))
        val problemSearchDto = AdminProblemSearchDto(1L, "title", "description", pageable)

        every {
            multipleChoiceProblemRepository.findMultipleChoiceProblemsByQuery(1L, "title", "description", pageable)
        } returns pagedProblems

        // when
        val result = adminMultipleProblemService.findProblems(problemSearchDto)

        // then
        verify {
            multipleChoiceProblemRepository.findMultipleChoiceProblemsByQuery(1L, "title", "description", pageable)
        }
        assertEquals(pagedProblems.totalElements, result.totalElements)
        assertEquals(pagedProblems.totalPages, result.totalPages)
        assertEquals(pagedProblems.content.first().title, result.problems.first().title)
    }

    @Test
    fun `findProblemById - 문제 없을시 예외 발생`() {
        // given
        every { multipleChoiceProblemRepository.findByIdOrNull(1L) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { adminMultipleProblemService.findProblemById(1L) }
        verify { multipleChoiceProblemRepository.findByIdOrNull(1L) }
    }

    @Test
    fun `findProblemById - 단일 문제 검색`() {
        // given
        every { multipleChoiceProblemRepository.findByIdOrNull(1L) } returns problem

        // when
        val result = adminMultipleProblemService.findProblemById(1L)

        // then
        verify { multipleChoiceProblemRepository.findByIdOrNull(1L) }
        assertEquals(result.id, problem.id)
        assertEquals(result.title, problem.title)
    }

    @Test
    fun `createProblem - 존재하지 않는 유저가 문제 생성할 시 예외 발생`() {
        // given
        val createRequestDto = MultipleChoiceProblemUpsertRequestDto(
            title = "title",
            description = "description",
            tags = mutableListOf(),
            choices = mutableListOf(),
            score = 10.0,
        )

        // when & then
        assertThrows<EntityNotFoundException> { adminMultipleProblemService.createProblem(createRequestDto, user) }
    }

    @Test
    fun `createProblem - 정답은 항상 1개 이상 존재해야 한다`() {
        // given
        val createRequestDto = MultipleChoiceProblemUpsertRequestDto(
            title = "title",
            description = "description",
            tags = mutableListOf(),
            choices = mutableListOf(),
            score = 10.0,
        )
        every { tagUpserter.setTags(any(), any()) } just runs

        // when & then
        assertThrows<ConditionConflictException> {
            adminMultipleProblemService.createProblem(createRequestDto, user)
        }

        verify { tagUpserter.setTags(any(), any()) }
    }

    @Test
    fun `createProblem - 문제 생성에 성공할 시 생성된 문제의 ID를 return 한다`() {
        // given
        val createRequestDto = MultipleChoiceProblemUpsertRequestDto(
            title = "title",
            description = "description",
            tags = mutableListOf(),
            choices = mutableListOf(ChoiceData(content = "content", isAnswer = true)),
            score = 10.0,
        )
        every { tagUpserter.setTags(any(), any()) } just runs
        every { problemRepository.save(any()) } returns problem

        // when & then
        val result = adminMultipleProblemService.createProblem(createRequestDto, user)
        verify { tagUpserter.setTags(any(), any()) }
        verify { problemRepository.save(any()) }
        assertEquals(problem.id, result)
    }

    @Test
    fun `updateProblem - 존재하지 않는 문제를 수정시 예외 발생`() {
        // given
        val requestDto = MultipleChoiceProblemUpsertRequestDto(
            title = "title",
            description = "description",
            tags = mutableListOf(),
            choices = mutableListOf(),
            score = 10.0,
        )
        every { multipleChoiceProblemRepository.findByIdOrNull(any()) } returns null

        // when & then
        assertThrows<EntityNotFoundException> {
            adminMultipleProblemService.updateProblem(1L, requestDto)
        }
        verify { multipleChoiceProblemRepository.findByIdOrNull(any()) }
    }

    @Test
    fun `updateProblem - choice를 수정시 정답이 존재하지 않으면 예외 발생`() {
        // given
        val requestDto = MultipleChoiceProblemUpsertRequestDto(
            title = "title",
            description = "description",
            tags = mutableListOf(),
            choices = mutableListOf(),
            score = 10.0,
        )
        every { multipleChoiceProblemRepository.findByIdOrNull(problem.id) } returns problem

        // when & then
        assertThrows<ConditionConflictException> {
            adminMultipleProblemService.updateProblem(1L, requestDto)
        }

        verify { multipleChoiceProblemRepository.findByIdOrNull(problem.id) }
    }
}
