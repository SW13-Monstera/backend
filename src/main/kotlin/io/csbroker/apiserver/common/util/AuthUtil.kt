package io.csbroker.apiserver.common.util

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User

fun getEmailFromSecurityContextHolder() = runCatching {
    val principal = SecurityContextHolder.getContext().authentication.principal as User
    principal.username
}.getOrNull()
