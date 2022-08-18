package com.csbroker.apiserver.common.client

import com.csbroker.apiserver.common.util.HEADER_AUTHORIZATION
import com.csbroker.apiserver.dto.user.GithubEmailResponseDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "github", url = "\${feign.github.url}")
interface GithubClient {
    @GetMapping("/user/emails")
    fun getUserEmail(@RequestHeader(HEADER_AUTHORIZATION) accessToken: String): List<GithubEmailResponseDto>
}
