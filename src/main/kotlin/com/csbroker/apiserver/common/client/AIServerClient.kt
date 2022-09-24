package com.csbroker.apiserver.common.client

import com.csbroker.apiserver.dto.problem.grade.GradingRequestDto
import com.csbroker.apiserver.dto.problem.grade.GradingResponseDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "ai", url = "\${feign.ai.url}")
interface AIServerClient {

    @PostMapping("/integrate_predict")
    fun getGrade(@RequestBody gradingRequestDto: GradingRequestDto): GradingResponseDto
}
