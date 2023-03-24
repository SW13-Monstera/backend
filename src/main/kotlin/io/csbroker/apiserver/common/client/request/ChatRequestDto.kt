package io.csbroker.apiserver.common.client.request

data class ChatRequestDto(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatRequestMessage>,
    val user: String,
) {
    data class ChatRequestMessage(
        val role: String = "user",
        val content: String,
    )
}
