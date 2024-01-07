package io.csbroker.apiserver.service.problem.admin

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import io.csbroker.apiserver.model.ShortProblem
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.problem.ProblemRepository
import io.csbroker.apiserver.repository.problem.ShortProblemRepository
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

class AdminShortProblemServiceTest {

    private lateinit var shortProblemRepository: ShortProblemRepository
    private lateinit var problemRepository: ProblemRepository
    private lateinit var tagUpserter: TagUpserter
    private lateinit var adminShortProblemService: AdminShortProblemService
    private lateinit var user: User
    private lateinit var problem: ShortProblem

    @BeforeEach
    fun setUp() {
        shortProblemRepository = mockk()
        problemRepository = mockk()
        tagUpserter = mockk()
        adminShortProblemService = AdminShortProblemServiceImpl(
            shortProblemRepository,
            problemRepository,
            tagUpserter,
        )
        user = User(
            id = UUID.randomUUID(),
            email = "test@test.com",
            password = "test1234!",
            username = "test",
            providerType = ProviderType.LOCAL,
        )
        problem = ShortProblem(
            title = "title",
            description = "description",
            creator = user,
            score = 10.0,
            answer = "answer",
        )
        problem.id = 1L
    }

    @Test
    fun `findProblems - success`() {
        // given
        val searchDto = AdminProblemSearchDto(1L, "title", "description", PageRequest.of(0, 10))
        val page = PageImpl(listOf(problem), searchDto.pageable, 1L)
        every { shortProblemRepository.findShortProblemsByQuery(any(), any(), any(), any()) } returns page

        // when
        val result = adminShortProblemService.findProblems(searchDto)

        // then
        verify { shortProblemRepository.findShortProblemsByQuery(any(), any(), any(), any()) }
        assertEquals(listOf(problem).size, result.problems.size)
        assertEquals(1, result.totalPages)
        assertEquals(1L, result.totalElements)
    }

    @Test
    fun `findProblems - 검색된 문제가 없을 경우 빈 페이지 반환`() {
        // given
        val searchDto = AdminProblemSearchDto(1L, "title", "description", PageRequest.of(0, 10))
        val page = PageImpl(emptyList<ShortProblem>(), searchDto.pageable, 0)
        every { shortProblemRepository.findShortProblemsByQuery(any(), any(), any(), any()) } returns page

        // when
        val result = adminShortProblemService.findProblems(searchDto)

        // then
        verify { shortProblemRepository.findShortProblemsByQuery(any(), any(), any(), any()) }
        assertEquals(true, result.problems.isEmpty())
    }

    @Test
    fun `findProblemById - success`() {
        // given
        every { shortProblemRepository.findByIdOrNull(any()) } returns problem

        // when
        val result = adminShortProblemService.findProblemById(1L)

        // then
        verify { shortProblemRepository.findByIdOrNull(any()) }
        assertEquals(1L, result.id)
    }

    @Test
    fun `findProblemById - 몬재하지 않는 문제 검색시 예외 발생`() {
        // given
        every { shortProblemRepository.findByIdOrNull(any()) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { adminShortProblemService.findProblemById(1L) }
        verify { shortProblemRepository.findByIdOrNull(any()) }
    }

    @Test
    fun `createProblem - success`() {
        // given
        val requestDto = ShortProblemUpsertRequestDto(
            title = "title",
            description = "description",
            tags = mutableListOf(),
            answer = "answer",
            score = 10.0,
        )
        every { problemRepository.save(any()) } returns problem
        every { tagUpserter.setTags(any(), any()) } just runs
        every { problemRepository.save(any()) } returns problem

        // when
        val result = adminShortProblemService.createProblem(requestDto, user)

        // then
        verify { problemRepository.save(any()) }
        verify { tagUpserter.setTags(any(), any()) }
        verify { problemRepository.save(any()) }
        assertEquals(problem.id, result)
    }

    @Test
    fun `updateProblem - success`() {
        // given
        val requestDto = ShortProblemUpsertRequestDto(
            title = "updatedTitle",
            description = "updatedDescription",
            tags = mutableListOf(),
            answer = "updatedAnswer",
            score = 10.0,
        )
        val email = "test@test.com"
        every { shortProblemRepository.findByIdOrNull(any()) } returns problem
        every { tagUpserter.updateTags(any(), any()) } just runs

        // when
        val result = adminShortProblemService.updateProblem(1L, requestDto)

        // then
        verify { shortProblemRepository.findByIdOrNull(any()) }
        verify { tagUpserter.updateTags(any(), any()) }
        assertEquals(problem.id, result)
    }

    @Test
    fun `updateProblem - 존재하지 않는 문제를 수정하면 예외가 발생합니다`() {
        // given
        every { shortProblemRepository.findByIdOrNull(any()) } returns null

        // when & then
        assertThrows<EntityNotFoundException> {
            adminShortProblemService.updateProblem(1L, mockk<ShortProblemUpsertRequestDto>())
        }
        verify { shortProblemRepository.findByIdOrNull(any()) }
    }
}
