package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.UserUpdateRequestDto
import com.csbroker.apiserver.model.User
import java.util.UUID

interface UserService {
    fun findUserByEmail(email: String): User?

    fun findUserById(uuid: UUID): User?

    fun modifyUser(uuid: UUID, userUpdateRequestDto: UserUpdateRequestDto): User?

    fun findUsers(): List<User>
}
