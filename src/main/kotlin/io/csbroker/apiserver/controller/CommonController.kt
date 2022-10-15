package io.csbroker.apiserver.controller

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.dto.StatsDto
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.common.RankListDto
import io.csbroker.apiserver.service.CommonService
import io.csbroker.apiserver.service.S3Service
import io.csbroker.apiserver.service.UserService
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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

    @PostMapping("/upload/image")
    fun uploadImg(@RequestPart("image") multipartFile: MultipartFile, @LoginUser loginUser: User): ApiResponse<String> {
        return runBlocking {
            val imgUrl = s3Service.uploadProfileImg(multipartFile)
            userService.updateUserProfileImg(loginUser.username, imgUrl)
            ApiResponse.success(imgUrl)
        }
    }

    @GetMapping("/techs")
    fun getTechs(@RequestParam("query", required = true) query: String): ApiResponse<List<String>> {
        return ApiResponse.success(commonService.findTechByQuery(query))
    }

    @GetMapping("/majors")
    fun getMajors(@RequestParam("query", required = true) query: String): ApiResponse<List<String>> {
        return ApiResponse.success(commonService.findMajorByQuery(query))
    }

    @GetMapping("/ranks")
    fun getRanks(
        @RequestParam("size", required = false, defaultValue = "10") size: Long,
        @RequestParam("page", required = false, defaultValue = "0") page: Long
    ): ApiResponse<RankListDto> {
        return ApiResponse.success(commonService.getRanks(size, page))
    }
}
