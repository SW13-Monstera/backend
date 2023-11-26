package io.csbroker.apiserver.service.user

import io.csbroker.apiserver.dto.user.UserStatsDto
import io.csbroker.apiserver.dto.user.UserUpdateRequestDto
import io.csbroker.apiserver.model.User
import java.util.UUID

interface UserService {
    fun findUserById(uuid: UUID): User?

    fun modifyUser(uuid: UUID, user: User, userUpdateRequestDto: UserUpdateRequestDto): User

    fun findUsers(): List<User>

    fun findAdminUsers(): List<User>
    fun getStats(id: UUID): UserStatsDto
    fun deleteUser(user: User, id: UUID): Boolean
    fun updateUserProfileImg(user: User, imgUrl: String)

    fun calculateRank()
}
