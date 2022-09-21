package com.csbroker.apiserver.controller

import com.csbroker.apiserver.auth.LoginUser
import com.csbroker.apiserver.dto.StatsDto
import com.csbroker.apiserver.dto.common.ApiResponse
import com.csbroker.apiserver.service.CommonService
import com.csbroker.apiserver.service.S3Service
import com.csbroker.apiserver.service.UserService
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.userdetails.User
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
    private val s3Service: S3Service,
    private val userService: UserService
) {
    @GetMapping("/stats")
    fun getStats(): ApiResponse<StatsDto> {
        return ApiResponse.success(commonService.getStats())
    }

    @PostMapping("/upload")
    fun uploadImg(@RequestPart("image") multipartFile: MultipartFile, @LoginUser loginUser: User): ApiResponse<String> {
        return runBlocking {
            val imgUrl = s3Service.uploadProfileImg(multipartFile)
            userService.updateUserProfileImg(loginUser.username, imgUrl)
            ApiResponse.success(imgUrl)
        }
    }
}
