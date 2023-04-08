package io.csbroker.apiserver.common.client

import io.csbroker.apiserver.dto.problem.grade.GradingResponseDto
import io.csbroker.apiserver.dto.problem.grade.KeywordGradingRequestDto
import io.csbroker.apiserver.dto.problem.grade.KeywordGradingResponseDto
import io.csbroker.apiserver.dto.problem.grade.LongProblemGradingRequestToAiServerDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "ai", url = "\${feign.ai.url}")
interface AIServerClient {

    @PostMapping("/integrate_predict")
    fun getGrade(@RequestBody gradingRequestDto: LongProblemGradingRequestToAiServerDto): GradingResponseDto

    @PostMapping("/keyword_predict")
    fun getKeywordGrade(@RequestBody keywordGradingRequestDto: KeywordGradingRequestDto): KeywordGradingResponseDto
}
