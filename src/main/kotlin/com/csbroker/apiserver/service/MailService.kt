package com.csbroker.apiserver.service

interface MailService {
    suspend fun sendPasswordChangeMail(to: String)
}
