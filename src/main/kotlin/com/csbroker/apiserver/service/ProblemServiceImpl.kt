package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.ProblemResponseDto
import com.csbroker.apiserver.dto.ProblemSearchDto
import com.csbroker.apiserver.model.Problem
import com.csbroker.apiserver.model.ProblemTag
import com.csbroker.apiserver.model.Tag
import com.csbroker.apiserver.repository.ProblemRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProblemServiceImpl : ProblemService {
    @Autowired
    private lateinit var problemRepository: ProblemRepository

    override fun findProblems(problemSearchDto: ProblemSearchDto): List<ProblemResponseDto> {
        return problemRepository.findByTitleContainingIgnoreCase(problemSearchDto.query)
            .filter {
                it.problemTags.map(ProblemTag::tag).map(Tag::name)
                    .toList().containsAll(problemSearchDto.tags)
            }.filter {
                it.gradingHistory.any { gh ->
                    gh.problem == it && gh.user.username == problemSearchDto.solvedBy
                }
            }.sortedByDescending {
                it.createdAt
            }.map(Problem::toProblemResponseDto)
    }

    override fun findProblemById(id: UUID): ProblemDetailResponseDto? {
        return problemRepository.findByIdOrNull(id)?.toProblemDetailResponseDto()
    }
}
