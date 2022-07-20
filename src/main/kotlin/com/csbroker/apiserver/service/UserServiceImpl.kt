package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.UserUpdateRequestDto
import com.csbroker.apiserver.model.User
import com.csbroker.apiserver.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID
import javax.transaction.Transactional

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder
) : UserService {
    override fun findUserByEmail(email: String): User? {
        return this.userRepository.findByEmail(email)
    }

    override fun findUserById(uuid: UUID): User? {
        return this.userRepository.findByIdOrNull(uuid)
    }

    @Transactional
    override fun modifyUser(uuid: UUID, userUpdateRequestDto: UserUpdateRequestDto): User? {
        val findUser = this.userRepository.findByIdOrNull(uuid)
            ?: return null

        if (userUpdateRequestDto.password != null) {
            val encodedPassword = bCryptPasswordEncoder.encode(userUpdateRequestDto.password)
            userUpdateRequestDto.password = encodedPassword
        }

        findUser.updateInfo(userUpdateRequestDto)

        return findUser
    }

    override fun findUsers(): List<User> {
        return this.userRepository.findAll()
    }
}
