package com.csbroker.apiserver.service

import com.csbroker.apiserver.common.enums.ErrorCode
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.common.exception.EntityNotFoundException
import com.csbroker.apiserver.dto.user.UserStatsDto
import com.csbroker.apiserver.common.exception.UnAuthorizedException
import com.csbroker.apiserver.dto.user.UserUpdateRequestDto
import com.csbroker.apiserver.model.GradingHistory
import com.csbroker.apiserver.model.User
import com.csbroker.apiserver.repository.GradingHistoryRepository
import com.csbroker.apiserver.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID
import javax.transaction.Transactional

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder,
    private val gradingHistoryRepository: GradingHistoryRepository
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

    override fun getStats(id: UUID, email: String): UserStatsDto {
        val findUser = this.userRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}를 가진 유저를 찾을 수 없습니다.")

        if (findUser.email != email) {
            throw EntityNotFoundException("${email}를 가진 유저를 찾을 수 없습니다.")
        }

        val gradingHistories = this.gradingHistoryRepository.findGradingHistoriesByUserId(findUser.id!!)

        val resultMap: MutableMap<Long, GradingHistory> = mutableMapOf()

        for (gradingHistory in gradingHistories) {
            if (resultMap[gradingHistory.problem.id!!] == null) {
                resultMap[gradingHistory.problem.id!!] = gradingHistory
            } else {
                if (resultMap[gradingHistory.problem.id!!]!!.score < gradingHistory.score) {
                    resultMap[gradingHistory.problem.id!!] = gradingHistory
                }
            }
        }

        val correctAnsweredMap = resultMap.filter {
            it.value.score == it.value.problem.score
        }

        val counter = mutableMapOf<String, Int>()

        correctAnsweredMap.forEach { correctAnswer ->
            correctAnswer.value.problem.problemTags.forEach {
                if (counter[it.tag.name] == null) {
                    counter[it.tag.name] = 1
                } else {
                    counter[it.tag.name] = counter[it.tag.name]!! + 1
                }
            }
        }

        val correctAnswered = correctAnsweredMap.map {
            UserStatsDto.ProblemStatsDto(it.key, it.value.problem.dtype, it.value.problem.title)
        }.toList()

        val wrongAnswered = resultMap.filter {
            it.value.score == 0.0
        }.map {
            UserStatsDto.ProblemStatsDto(it.key, it.value.problem.dtype, it.value.problem.title)
        }.toList()

        val partialAnswered = resultMap.filter {
            it.value.score != 0.0 && it.value.score != it.value.problem.score && it.value.problem.dtype == "long"
        }.map {
            UserStatsDto.ProblemStatsDto(it.key, it.value.problem.dtype, it.value.problem.title)
        }.toList()

        return UserStatsDto(
            correctAnswered,
            wrongAnswered,
            partialAnswered,
            counter
        )
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
