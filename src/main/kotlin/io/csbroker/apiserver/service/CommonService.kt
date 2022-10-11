package io.csbroker.apiserver.service

import io.csbroker.apiserver.dto.StatsDto
import io.csbroker.apiserver.dto.common.RankListDto
import io.csbroker.apiserver.dto.problem.ProblemResponseDto

interface CommonService {
    fun getStats(): StatsDto
    fun getTodayProblems(): List<ProblemResponseDto>
    fun getRanks(size: Long, page: Long): RankListDto
    fun findTechByQuery(query: String): List<String>
    fun findMajorByQuery(query: String): List<String>
}
