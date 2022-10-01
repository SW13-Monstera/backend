package io.csbroker.apiserver.repository

import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    @Query("select u from User u where u.email = :email and u.isDeleted = FALSE")
    fun findByEmail(@Param("email") email: String): User?

    @Query("select u from User u where u.id = :id and u.isDeleted = FALSE")
    fun findByIdOrNull(@Param("id") id: UUID): User?

    fun findByEmailOrUsername(email: String, username: String): User?

    fun findUsersByRole(role: Role): List<User>

    fun findUserByProviderId(providerId: String): User?

    @Query("select count(u.id) > 0 from User u where u.email = :email")
    fun existsUserByEmail(@Param("email") email: String): Boolean

    @Query("select count(u) from User u where u.isDeleted = FALSE")
    fun countUser(): Long
}
