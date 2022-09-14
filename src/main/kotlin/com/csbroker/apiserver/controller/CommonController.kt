package com.csbroker.apiserver.controller

import com.csbroker.apiserver.dto.StatsDto
import com.csbroker.apiserver.dto.common.ApiResponse
import com.csbroker.apiserver.service.CommonService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class CommonController(
    private val commonService: CommonService
) {
    @GetMapping("/stats")
    fun getStats(): ApiResponse<StatsDto> {
        return ApiResponse.success(commonService.getStats())
    }
}
