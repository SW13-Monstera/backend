package com.csbroker.apiserver.common.util

import com.csbroker.apiserver.dto.problem.GradingRequestDto
import com.csbroker.apiserver.dto.problem.GradingResponseDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "ai", url = "\${feign.ai.url}")
interface AIServerClient {

    @PostMapping("/keyword_predict")
    fun getGrade(@RequestBody gradingRequestDto: GradingRequestDto): GradingResponseDto
}
