package com.csbroker.apiserver.service

import com.csbroker.apiserver.model.User
import com.csbroker.apiserver.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {
    override fun findUserByEmail(email: String): User? {
        return this.userRepository.findByEmail(email)
    }
}
