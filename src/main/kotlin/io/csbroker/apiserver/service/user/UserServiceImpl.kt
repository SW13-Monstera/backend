package io.csbroker.apiserver.service.user

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.dto.user.UserStatsDto
import io.csbroker.apiserver.dto.user.UserStatsDto.ProblemStatsDto
import io.csbroker.apiserver.dto.user.UserUpdateRequestDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.common.RedisRepository
import io.csbroker.apiserver.repository.problem.GradingHistoryRepository
import io.csbroker.apiserver.repository.user.UserRepository
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val gradingHistoryRepository: GradingHistoryRepository,
    private val redisRepository: RedisRepository,
) : UserService {
    override fun findUserById(uuid: UUID): User? {
        return userRepository.findByIdOrNull(uuid)
    }

    @Transactional
    override fun modifyUser(uuid: UUID, user: User, userUpdateRequestDto: UserUpdateRequestDto): User {
        if (user.id != uuid) {
            throw UnAuthorizedException(ErrorCode.FORBIDDEN, "해당 유저를 수정할 권한이 없습니다.")
        }

        userUpdateRequestDto.password?.let {
            if (user.providerType != ProviderType.LOCAL) {
                throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "간편 가입 유저는 비밀번호를 변경할 수 없습니다.")
            }
            encodePassword(user, userUpdateRequestDto)
        }

        user.updateInfo(userUpdateRequestDto)

        return user
    }

    private fun encodePassword(user: User, userUpdateRequestDto: UserUpdateRequestDto) {
        if (!passwordEncoder.matches(userUpdateRequestDto.originalPassword, user.password)) {
            throw UnAuthorizedException(ErrorCode.PASSWORD_MISS_MATCH, "비밀번호가 일치하지 않습니다!")
        }
        userUpdateRequestDto.password = passwordEncoder.encode(userUpdateRequestDto.password)
    }

    override fun findUsers(): List<User> {
        return userRepository.findAll()
    }

    override fun findAdminUsers(): List<User> {
        return userRepository.findUsersByRole(Role.ROLE_ADMIN)
    }

    override fun getStats(id: UUID): UserStatsDto {
        val findUser = userRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}를 가진 유저를 찾을 수 없습니다.")

        val gradingHistories = gradingHistoryRepository.findGradingHistoriesByUserId(findUser.id!!)

        val resultMap = gradingHistories.groupBy {
            it.problem.id
        }.mapValues { v ->
            v.value.maxByOrNull { it.score } ?: v.value.first()
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
            ProblemStatsDto.from(it.key, it.value)
        }

        val wrongAnswered = resultMap.filter {
            it.value.score == 0.0
        }.map {
            ProblemStatsDto.from(it.key, it.value)
        }

        val partialAnswered = resultMap.filter {
            it.value.score != 0.0 && it.value.score != it.value.problem.score
        }.map {
            ProblemStatsDto.from(it.key, it.value)
        }

        val rankResultDto = redisRepository.getRank(makeRankKey(id, findUser.username))

        return UserStatsDto(
            correctAnswered,
            wrongAnswered,
            partialAnswered,
            tagCounterMap,
            rankResultDto.rank,
            rankResultDto.score,
        )
    }

    override fun deleteUser(user: User, id: UUID): Boolean {
        val findUserById = findUserById(id)
            ?: throw EntityNotFoundException("${id}를 가진 유저를 찾을 수 없습니다.")

        if (user.role == Role.ROLE_ADMIN || user.id == findUserById.id) {
            findUserById.isDeleted = true
            return true
        } else {
            throw UnAuthorizedException(ErrorCode.FORBIDDEN, "해당 유저를 삭제할 권한이 없습니다.")
        }
    }

    @Transactional
    override fun updateUserProfileImg(user: User, imgUrl: String) {
        user.profileImageUrl = imgUrl
    }

    @Scheduled(cron = "0 0 * * * *")
    override fun calculateRank() {
        val userScoreMap = userRepository.findAll().associate {
            val scoreMap = it.gradingHistories.groupBy { gradingHistory ->
                gradingHistory.problem.id
            }.mapValues { map ->
                map.value.maxOfOrNull { gradingHistory -> gradingHistory.score } ?: 0.0
            }
            makeRankKey(it.id!!, it.username) to scoreMap.values.sum()
        }

        redisRepository.setRank(userScoreMap)
    }

    private fun makeRankKey(id: UUID, username: String) = "$id@$username"
}
