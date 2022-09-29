package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.StatsDto
import com.csbroker.apiserver.dto.common.RankListDto
import com.csbroker.apiserver.dto.problem.ProblemResponseDto
import com.csbroker.apiserver.repository.ProblemRepository
import com.csbroker.apiserver.repository.TechRepository
import com.csbroker.apiserver.repository.UserRepository
import com.csbroker.apiserver.repository.common.RedisRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommonServiceImpl(
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository,
    private val techRepository: TechRepository,
    private val redisRepository: RedisRepository
) : CommonService {

    override fun getStats(): StatsDto {
        val problemCnt = problemRepository.count()
        val gradableProblemCnt = problemRepository.countGradableProblem()
        val userCnt = userRepository.countUser()

        return StatsDto(problemCnt, gradableProblemCnt, userCnt)
    }

    override fun getTodayProblems(): List<ProblemResponseDto> {
        TODO("Not yet implemented")
    }

    override fun getRanks(size: Long, page: Long): RankListDto {
        return redisRepository.getRanks(size, page)
    }

    override fun findTechByQuery(query: String): List<String> {
        return this.techRepository.findByNameContainingIgnoreCase(query).map {
            it.name
        }
    }
}
