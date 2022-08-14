package com.csbroker.apiserver.common.util

import com.csbroker.apiserver.dto.problem.GradingRequestDto
import com.csbroker.apiserver.dto.problem.GradingResponseDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "ai", url = "http://monstera-ai-nlb-f2edf7b47553e838.elb.ap-northeast-2.amazonaws.com")
interface AIServerClient {

    @PostMapping("/keyword_predict")
    fun getGrade(@RequestBody gradingRequestDto: GradingRequestDto): GradingResponseDto
}
