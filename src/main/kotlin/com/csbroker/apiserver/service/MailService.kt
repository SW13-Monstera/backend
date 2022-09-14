package com.csbroker.apiserver.service

interface MailService {
    fun sendPasswordChangeMail(to: String)
}
