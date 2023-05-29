package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetUpsertRequestDto
import io.csbroker.apiserver.model.Problem
import io.csbroker.apiserver.model.ProblemSet
import io.csbroker.apiserver.model.ProblemSetMapping
import io.csbroker.apiserver.repository.problem.ProblemRepository
import io.csbroker.apiserver.repository.problem.ProblemSetMappingRepository
import io.csbroker.apiserver.repository.problem.ProblemSetRepository
import org.junit.jupiter.api.Assertions.assertEquals
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull

class ProblemSetServiceTest {

    private val problemSetRepository = mockk<ProblemSetRepository>()
    private val problemRepository = mockk<ProblemRepository>()
    private val problemSetMappingRepository = mockk<ProblemSetMappingRepository>()
    private lateinit var service: ProblemSetService

    @BeforeEach
    fun setUp() {
        service = ProblemSetServiceImpl(
            problemSetRepository,
            problemRepository,
            problemSetMappingRepository,
        )
    }

    @Test
    fun `findById - 없는 문제셋을 조회시 예외가 발생합니다 `() {
        // given
        every { problemSetRepository.findByIdOrNull(any()) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.findById(1L) }
        verify { problemSetRepository.findByIdOrNull(any()) }

    }

    @Test
    fun `createProblemSet - 요청에 있는 문제 아이디 수와 실제 존재하는 문제의 수가 다르면 예외가 발생합니다`() {
        // given
        val problemIds = listOf(1L, 2L, 3L)
        val existProblems = listOf(mockk<Problem>(), mockk<Problem>())
        val name = "problemSetName"
        val description = "description"
        val requestDto = ProblemSetUpsertRequestDto(problemIds, name, description)

        val problemSetId = 1L
        val problemSet = ProblemSet(problemSetId, name, description)
        every { problemSetRepository.save(any()) } returns problemSet
        every { problemRepository.findAllById(problemIds) } returns existProblems

        // when & then
        assertThrows<EntityNotFoundException> { service.createProblemSet(requestDto) }
        verify { problemSetRepository.save(any()) }
        verify { problemRepository.findAllById(problemIds) }

    }

    @Test
    fun `createProblemSet - success`() {
        // given
        val problemIds = listOf(1L, 2L, 3L)
        val existProblems = listOf(mockk<Problem>(), mockk<Problem>(), mockk<Problem>())
        val name = "problemSetName"
        val description = "description"
        val requestDto = ProblemSetUpsertRequestDto(problemIds, name, description)

        val problemSetId = 1L
        val problemSet = ProblemSet(problemSetId, name, description)
        val problemSetMappings = existProblems.map{
            ProblemSetMapping(
                problem = it,
                problemSet = problemSet,
            )
        }
        every { problemSetRepository.save(any()) } returns problemSet
        every { problemRepository.findAllById(problemIds) } returns existProblems
        every { problemSetMappingRepository.saveAll(any<List<ProblemSetMapping>>()) } returns problemSetMappings
        // when
        val result = service.createProblemSet(requestDto)

        // when & then
        assertEquals(problemSetId, result)
        verify { problemSetRepository.save(any()) }
        verify { problemRepository.findAllById(problemIds) }
    }

    @Test
    fun `updateProblemSet - 없는 문제 셋을 수정하려하면 예외가 발생합니다`() {
        // given
        val requestDto = mockk<ProblemSetUpsertRequestDto>()
        every { problemSetRepository.findByIdOrNull(1L) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.updateProblemSet(1L, requestDto) }
        verify { problemSetRepository.findByIdOrNull(1L) }
    }

    @Test
    fun `updateProblemSet - 수정하려는 문제셋중에 없는 문제 ID가 존재하면 예외가 발생합니다 `() {
        // given
        val problemIds = listOf(1L, 2L, 3L)
        val existProblems = listOf(mockk<Problem>(), mockk<Problem>())
        val name = "problemSetName"
        val description = "description"
        val requestDto = ProblemSetUpsertRequestDto(problemIds, name, description)

        val problemSetId = 1L
        val problemSet = ProblemSet(problemSetId, name, description)
        every { problemSetRepository.findByIdOrNull(problemSetId) } returns problemSet
        every { problemRepository.findAllById(problemIds) } returns existProblems
        every { problemSetMappingRepository.deleteAllByProblemSetId(problemSetId) } just runs

        // when & then
        assertThrows<EntityNotFoundException> { service.updateProblemSet(1L, requestDto) }
        verify { problemSetRepository.findByIdOrNull(problemSetId) }
        verify { problemRepository.findAllById(problemIds) }
        verify { problemSetMappingRepository.deleteAllByProblemSetId(problemSetId) }

    }

    @Test
    fun `updateProblemSet - success `() {
        // given
        val problemIds = listOf(1L, 2L, 3L)
        val existProblems = listOf(mockk<Problem>(), mockk<Problem>(), mockk<Problem>())
        val name = "problemSetName"
        val description = "description"
        val requestDto = ProblemSetUpsertRequestDto(problemIds, name, description)

        val problemSetId = 1L
        val problemSet = ProblemSet(problemSetId, name, description)
        val problemSetMappings = existProblems.map{
            ProblemSetMapping(
                problem = it,
                problemSet = problemSet,
            )
        }
        every { problemSetRepository.findByIdOrNull(problemSetId) } returns problemSet
        every { problemSetMappingRepository.deleteAllByProblemSetId(problemSetId) } just runs
        every { problemRepository.findAllById(problemIds) } returns existProblems
        every { problemSetMappingRepository.saveAll(any<List<ProblemSetMapping>>()) } returns problemSetMappings

        // when & then
        val result = service.updateProblemSet(1L, requestDto)
        assertEquals(problemSetId, result)
        verify { problemSetRepository.findByIdOrNull(problemSetId) }
        verify { problemSetMappingRepository.deleteAllByProblemSetId(problemSetId) }
        verify { problemRepository.findAllById(problemIds) }
        verify { problemSetMappingRepository.saveAll(any<List<ProblemSetMapping>>()) }

    }

}
