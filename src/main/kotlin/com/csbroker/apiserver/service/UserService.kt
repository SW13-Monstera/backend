package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.user.UserStatsDto
import com.csbroker.apiserver.dto.user.UserUpdateRequestDto
import com.csbroker.apiserver.model.User
import java.util.UUID

interface UserService {
    fun findUserByEmail(email: String): User?

    fun findUserById(uuid: UUID): User?

    fun modifyUser(uuid: UUID, userUpdateRequestDto: UserUpdateRequestDto): User

    fun findUsers(): List<User>

    fun findAdminUsers(): List<User>
    fun getStats(id: UUID, email: String): UserStatsDto
    fun deleteUser(email: String, id: UUID): Boolean
    fun updateUserProfileImg(email: String, imgUrl: String)

    fun calculateRank()
}
