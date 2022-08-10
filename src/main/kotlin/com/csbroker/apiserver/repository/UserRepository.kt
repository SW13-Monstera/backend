package com.csbroker.apiserver.repository

import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun findByEmailOrUsername(email: String, username: String): User?
    fun findUsersByRole(role: Role): List<User>
    fun findUserByProviderId(providerId: String): User?
}
