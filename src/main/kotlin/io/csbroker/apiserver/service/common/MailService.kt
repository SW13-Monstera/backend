package io.csbroker.apiserver.service.common

interface MailService {
    suspend fun sendPasswordChangeMail(email: String)
}
