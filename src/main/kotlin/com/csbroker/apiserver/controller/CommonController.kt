package com.csbroker.apiserver.controller

import com.csbroker.apiserver.dto.StatsDto
import com.csbroker.apiserver.dto.common.ApiResponse
import com.csbroker.apiserver.service.CommonService
import com.csbroker.apiserver.service.S3Service
import kotlinx.coroutines.runBlocking
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1")
class CommonController(
    private val commonService: CommonService,
    private val s3Service: S3Service
) {
    @GetMapping("/stats")
    fun getStats(): ApiResponse<StatsDto> {
        return ApiResponse.success(commonService.getStats())
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'BUSINESS')")
    fun uploadImg(@RequestPart("image") multipartFile: MultipartFile): ApiResponse<String> {
        return runBlocking {
            ApiResponse.success(s3Service.uploadProfileImg(multipartFile))
        }
    }
}
