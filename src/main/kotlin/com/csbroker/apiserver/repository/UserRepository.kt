package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID>
