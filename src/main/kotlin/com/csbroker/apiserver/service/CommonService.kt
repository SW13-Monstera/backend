package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.StatsDto
import com.csbroker.apiserver.dto.common.RankListDto
import com.csbroker.apiserver.dto.problem.ProblemResponseDto

interface CommonService {
    fun getStats(): StatsDto
    fun getTodayProblems(): List<ProblemResponseDto>
    fun getRanks(start: Long, end: Long): RankListDto
    fun findTechByQuery(query: String): List<String>
}
