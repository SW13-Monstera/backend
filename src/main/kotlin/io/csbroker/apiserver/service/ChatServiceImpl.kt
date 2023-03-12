package io.csbroker.apiserver.service

import io.csbroker.apiserver.common.client.OpenAiClient
import io.csbroker.apiserver.common.client.request.ChatRequestDto
import io.csbroker.apiserver.common.util.TOKEN_PREFIX
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ChatServiceImpl(
    private val openAiClient: OpenAiClient,
    @Value("\${openai.key}")
    private val apiKey: String,
) : ChatService {
    override fun completeChat(email: String, content: String): String {
        val response = openAiClient.chatCompletion(
            "$TOKEN_PREFIX $apiKey",
            ChatRequestDto(
                messages = listOf(
                    ChatRequestDto.ChatRequestMessage(content = content, user = email),
                ),
            ),
        )

        return response.getChatResponseAnswer()
    }
}
