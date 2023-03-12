package io.csbroker.apiserver.service

interface ChatService {
    fun completeChat(email: String, content: String): String
}
