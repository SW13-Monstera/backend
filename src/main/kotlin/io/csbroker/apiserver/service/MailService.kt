package io.csbroker.apiserver.service

interface MailService {
    suspend fun sendPasswordChangeMail(to: String)
}
