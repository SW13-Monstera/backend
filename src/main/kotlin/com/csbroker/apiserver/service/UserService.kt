package com.csbroker.apiserver.service

import com.csbroker.apiserver.model.User

interface UserService {
    fun findUserByEmail(email: String): User?
}
