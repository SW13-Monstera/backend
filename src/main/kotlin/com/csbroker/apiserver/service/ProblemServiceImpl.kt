package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.ProblemResponseDto
import com.csbroker.apiserver.dto.ProblemSearchDto
import com.csbroker.apiserver.model.Problem
import com.csbroker.apiserver.repository.ProblemRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProblemServiceImpl(
    private val problemRepository: ProblemRepository
) : ProblemService {

    override fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): List<ProblemResponseDto> {
        return this.problemRepository.findProblemsByQuery(problemSearchDto, pageable)
            .map(Problem::toProblemResponseDto)
    }

    override fun findProblemById(id: UUID): ProblemDetailResponseDto? {
        return problemRepository.findByIdOrNull(id)?.toProblemDetailResponseDto()
    }
}
