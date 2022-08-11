package com.csbroker.apiserver.common.util

import com.csbroker.apiserver.dto.user.GithubEmailResponseDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "github", url = "https://api.github.com")
interface GithubClient {

    @GetMapping("/user/emails")
    fun getUserEmail(@RequestHeader(HEADER_AUTHORIZATION) accessToken: String): List<GithubEmailResponseDto>
}
