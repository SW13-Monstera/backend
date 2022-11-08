package io.csbroker.apiserver.service

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.dto.user.UserStatsDto
import io.csbroker.apiserver.dto.user.UserUpdateRequestDto
import io.csbroker.apiserver.model.GradingHistory
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.GradingHistoryRepository
import io.csbroker.apiserver.repository.UserRepository
import io.csbroker.apiserver.repository.common.RedisRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID
import javax.transaction.Transactional
import kotlin.math.max

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder,
    private val gradingHistoryRepository: GradingHistoryRepository,
    private val redisRepository: RedisRepository
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

    override fun getStats(id: UUID): UserStatsDto {
        val findUser = this.userRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}를 가진 유저를 찾을 수 없습니다.")

        val gradingHistories = this.gradingHistoryRepository.findGradingHistoriesByUserId(findUser.id!!)

        val resultMap = mutableMapOf<Long, GradingHistory>()

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

        val tagCounterMap = correctAnsweredMap.values.flatMap {
            it.problem.problemTags
        }.map {
            it.tag
        }.groupingBy {
            it.name
        }.eachCount()

        val correctAnswered = correctAnsweredMap.map {
            UserStatsDto.ProblemStatsDto(it.key, it.value.problem.dtype, it.value.problem.title)
        }.toList()

        val wrongAnswered = resultMap.filter {
            it.value.score == 0.0
        }.map {
            UserStatsDto.ProblemStatsDto(it.key, it.value.problem.dtype, it.value.problem.title)
        }.toList()

        val partialAnswered = resultMap.filter {
            it.value.score != 0.0 && it.value.score != it.value.problem.score
        }.map {
            UserStatsDto.ProblemStatsDto(it.key, it.value.problem.dtype, it.value.problem.title)
        }.toList()

        val rankResultDto = redisRepository.getRank(id.toString() + '@' + findUser.username)

        return UserStatsDto(
            correctAnswered,
            wrongAnswered,
            partialAnswered,
            tagCounterMap,
            rankResultDto.rank,
            rankResultDto.score
        )
    }

    override fun deleteUser(email: String, id: UUID): Boolean {
        val findUserById = this.findUserById(id)
            ?: throw EntityNotFoundException("${id}를 가진 유저를 찾을 수 없습니다.")

        if (findUserById.email != email) {
            throw UnAuthorizedException(ErrorCode.FORBIDDEN, "해당 유저를 삭제할 권한이 없습니다.")
        }

        findUserById.isDeleted = true

        return true
    }

    @Transactional
    override fun updateUserProfileImg(email: String, imgUrl: String) {
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("${email}를 가진 유저를 찾을 수 없습니다.")

        findUser.profileImageUrl = imgUrl
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    override fun calculateRank() {
        val findUsers = userRepository.findAll()
        val allUserScoreMap = mutableMapOf<String, Double>()

        for (findUser in findUsers) {
            val scoreMap = mutableMapOf<Long, Double>()
            findUser.gradingHistories.forEach {
                scoreMap[it.problem.id!!] = max(scoreMap[it.problem.id!!] ?: 0.0, it.score)
            }
            allUserScoreMap[findUser.id!!.toString() + '@' + findUser.username] = scoreMap.values.sum()
        }

        redisRepository.setRank(allUserScoreMap)
    }
}
