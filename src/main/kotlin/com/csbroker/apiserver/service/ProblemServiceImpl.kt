package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.LongProblemCreateRequestDto
import com.csbroker.apiserver.dto.MultipleChoiceProblemCreateRequestDto
import com.csbroker.apiserver.dto.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.ProblemResponseDto
import com.csbroker.apiserver.dto.ProblemSearchDto
import com.csbroker.apiserver.dto.ShortProblemCreateRequestDto
import com.csbroker.apiserver.model.Problem
import com.csbroker.apiserver.repository.LongProblemRepository
import com.csbroker.apiserver.repository.MultipleChoiceProblemRepository
import com.csbroker.apiserver.repository.ProblemRepository
import com.csbroker.apiserver.repository.ShortProblemRepository
import com.csbroker.apiserver.repository.UserRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ProblemServiceImpl(
    private val problemRepository: ProblemRepository,
    private val shortProblemRepository: ShortProblemRepository,
    private val longProblemRepository: LongProblemRepository,
    private val multipleChoiceProblemRepository: MultipleChoiceProblemRepository,
    private val userRepository: UserRepository
) : ProblemService {

    override fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): List<ProblemResponseDto> {
        return this.problemRepository.findProblemsByQuery(problemSearchDto, pageable)
            .map(Problem::toProblemResponseDto)
    }

    override fun findProblemById(id: Long): ProblemDetailResponseDto? {
        return problemRepository.findByIdOrNull(id)?.toProblemDetailResponseDto()
    }

    override fun createLongProblem(createRequestDto: LongProblemCreateRequestDto, email: String): Long {
        val findUser = userRepository.findByEmail(email) ?: throw IllegalArgumentException("에러 발생")
        val longProblem = createRequestDto.toLongProblem(findUser)
        val gradingStandardList = createRequestDto.getGradingStandardList(longProblem)
        longProblem.addGradingStandards(gradingStandardList)

        return this.problemRepository.save(longProblem).id!!
    }

    override fun createShortProblem(createRequestDto: ShortProblemCreateRequestDto, email: String): Long {
        val findUser = userRepository.findByEmail(email) ?: throw IllegalArgumentException("에러 발생")
        return this.problemRepository.save(createRequestDto.toShortProblem(findUser)).id!!
    }

    override fun createMultipleChoiceProblem(
        createRequestDto: MultipleChoiceProblemCreateRequestDto,
        email: String
    ): Long {
        val findUser = userRepository.findByEmail(email) ?: throw IllegalArgumentException("에러 발생")
        val multipleChoiceProblem = createRequestDto.toMultipleChoiceProblem(findUser)
        val choiceDataList = createRequestDto.getChoiceList(multipleChoiceProblem)
        multipleChoiceProblem.addChoices(choiceDataList)

        return this.problemRepository.save(multipleChoiceProblem).id!!
    }
}
