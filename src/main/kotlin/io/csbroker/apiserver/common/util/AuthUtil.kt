package io.csbroker.apiserver.common.util

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User

fun getEmailFromSecurityContextHolder(): String? {
    return try {
        val principal = SecurityContextHolder.getContext().authentication.principal
            as User
        principal.username
    } catch (e: Exception) {
        null
    }
}
