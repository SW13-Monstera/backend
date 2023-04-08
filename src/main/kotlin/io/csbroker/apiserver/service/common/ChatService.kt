package io.csbroker.apiserver.service.common

interface ChatService {
    fun completeChat(email: String, content: String): String
}
