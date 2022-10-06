package io.csbroker.apiserver.service

import io.csbroker.apiserver.dto.StatsDto
import io.csbroker.apiserver.dto.common.RankListDto
import io.csbroker.apiserver.dto.problem.ProblemResponseDto
import io.csbroker.apiserver.repository.MajorRepository
import io.csbroker.apiserver.repository.ProblemRepository
import io.csbroker.apiserver.repository.TechRepository
import io.csbroker.apiserver.repository.UserRepository
import io.csbroker.apiserver.repository.common.RedisRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommonServiceImpl(
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository,
    private val techRepository: TechRepository,
    private val majorRepository: MajorRepository,
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

    override fun findMajorByQuery(query: String): List<String> {
        return this.majorRepository.findByNameContainingIgnoreCase(query).map {
            it.name
        }
    }
}