package io.csbroker.apiserver.service

import io.csbroker.apiserver.common.client.OpenAiClient
import io.csbroker.apiserver.common.client.request.ChatRequestDto
import io.csbroker.apiserver.common.util.TOKEN_PREFIX
import io.csbroker.apiserver.model.ChatResult
import io.csbroker.apiserver.repository.ChatResultRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ChatServiceImpl(
    private val openAiClient: OpenAiClient,
    @Value("\${openai.key}")
    private val apiKey: String,
    private val chatResultRepository: ChatResultRepository,
) : ChatService {
    override fun completeChat(email: String, content: String): String {
        val response = openAiClient.chatCompletion(
            "$TOKEN_PREFIX $apiKey",
            ChatRequestDto(
                messages = listOf(
                    ChatRequestDto.ChatRequestMessage(content = "$prefix $content"),
                ),
                user = email,
            ),
        )

        val answer = response.getChatResponseAnswer()
        chatResultRepository.save(ChatResult(email = email, question = content, answer = answer))
        return answer
    }

    companion object {
        private const val prefix: String = "Please answer me in korean as you were a veteran senior software engineer."
    }
}
