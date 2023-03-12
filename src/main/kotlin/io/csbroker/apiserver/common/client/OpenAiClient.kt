package io.csbroker.apiserver.common.client

import io.csbroker.apiserver.common.client.request.ChatRequestDto
import io.csbroker.apiserver.common.client.response.ChatResponseDto
import io.csbroker.apiserver.common.util.HEADER_AUTHORIZATION
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "openai", url = "https://api.openai.com")
interface OpenAiClient {
    @PostMapping("/v1/chat/completions")
    fun chatCompletion(
        @RequestHeader(HEADER_AUTHORIZATION) apiKey: String,
        @RequestBody chatRequestDto: ChatRequestDto,
    ): ChatResponseDto
}
