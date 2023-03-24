package io.csbroker.apiserver.common.client.response

data class ChatResponseDto(
    val id: String,
    val choices: List<ChatChoice>,
) {
    data class ChatChoice(
        val message: ChatMessage,
    )

    data class ChatMessage(
        val role: String,
        val content: String,
    )

    fun getChatResponseAnswer(): String {
        return choices.first().message.content
    }
}
