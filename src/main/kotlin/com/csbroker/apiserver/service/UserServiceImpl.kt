package com.csbroker.apiserver.service

import com.csbroker.apiserver.common.enums.ErrorCode
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.common.exception.EntityNotFoundException
import com.csbroker.apiserver.common.exception.UnAuthorizedException
import com.csbroker.apiserver.dto.user.UserUpdateRequestDto
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
    override fun modifyUser(uuid: UUID, userUpdateRequestDto: UserUpdateRequestDto): User {
        val findUser = this.userRepository.findByIdOrNull(uuid)
            ?: throw EntityNotFoundException("${uuid}를 가진 유저를 찾을 수 없습니다.")

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

    override fun findAdminUsers(): List<User> {
        return this.userRepository.findUsersByRole(Role.ROLE_ADMIN)
    }

    override fun deleteUser(email: String, id: UUID): Boolean {
        val findUserById = this.findUserById(id)
            ?: throw EntityNotFoundException("${id}를 가진 유저를 찾을 수 없습니다.")

        if (findUserById.email != email) {
            throw UnAuthorizedException(ErrorCode.UNAUTHORIZED, "해당 유저를 삭제할 권한이 없습니다.")
        }

        findUserById.isDeleted = true

        return true
    }
}
