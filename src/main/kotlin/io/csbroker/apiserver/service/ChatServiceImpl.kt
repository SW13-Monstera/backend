package io.csbroker.apiserver.service

import io.csbroker.apiserver.common.client.OpenAiClient
import io.csbroker.apiserver.common.client.request.ChatRequestDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ChatServiceImpl(
    private val openAiClient: OpenAiClient,
    @Value("\${openai.apikey}")
    private val apiKey: String,
) : ChatService {
    override fun completeChat(email: String, content: String): String {
        val response = openAiClient.chatCompletion(
            apiKey,
            ChatRequestDto(
                messages = listOf(
                    ChatRequestDto.ChatRequestMessage(content = content, user = email),
                ),
            ),
        )

        return response.getChatResponseAnswer()
    }
}
