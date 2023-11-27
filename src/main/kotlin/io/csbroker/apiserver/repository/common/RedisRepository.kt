package io.csbroker.apiserver.repository.common

import io.csbroker.apiserver.common.config.properties.AppProperties
import io.csbroker.apiserver.dto.common.RankListDto
import io.csbroker.apiserver.dto.user.RankResultDto
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.SessionCallback
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations.TypedTuple
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.TimeUnit

const val PASSWORD_VERIFICATION_MINUTE = 5
const val RANKING = "ranking"

@Repository
class RedisRepository(
    private val redisTemplate: StringRedisTemplate,
    private val appProperties: AppProperties,
) {
    fun getRefreshTokenByEmail(email: String): String? {
        return redisTemplate.opsForValue().get(email)
    }

    fun setRefreshTokenByEmail(email: String, refreshToken: String) {
        redisTemplate.opsForValue().set(email, refreshToken, appProperties.auth.refreshTokenExpiry, TimeUnit.MILLISECONDS)
    }

    fun setPasswordVerification(code: String, email: String) {
        redisTemplate.opsForValue().set(code, email, PASSWORD_VERIFICATION_MINUTE.toLong(), TimeUnit.MINUTES)
    }

    fun getEmailByCode(code: String): String? {
        return redisTemplate.opsForValue().get(code)
    }

    fun removePasswordVerification(code: String) {
        redisTemplate.delete(code)
    }

    @Suppress("UNCHECKED_CAST")
    fun setRank(scoreMap: Map<String, Double>) {
        redisTemplate.execute(
            object : SessionCallback<Unit> {
                override fun <K : Any?, V : Any?> execute(operations: RedisOperations<K, V>) {
                    val stringOperations = operations as RedisOperations<String, String>
                    stringOperations.multi()
                    stringOperations.delete(RANKING)
                    stringOperations.opsForZSet().add(
                        RANKING,
                        scoreMap.map { TypedTuple.of(it.key, it.value) }.toSet(),
                    )
                    operations.exec()
                }
            },
        )
    }

    fun getRank(key: String): RankResultDto {
        val score = redisTemplate.opsForZSet().score(RANKING, key) ?: 0.0
        val rankKey = redisTemplate.opsForZSet().reverseRangeByScore(RANKING, score, score, 0, 1)?.first()

        val rank = rankKey?.let {
            redisTemplate.opsForZSet().reverseRank(RANKING, rankKey)?.plus(1)
        }

        return RankResultDto(rank, score)
    }

    fun getRanks(size: Long, page: Long): RankListDto {
        val start = size * page
        val end = start + size - 1

        val keyWithScores = redisTemplate.opsForZSet().reverseRangeWithScores(RANKING, start, end) ?: emptySet()
        val totalElements = redisTemplate.opsForZSet().size(RANKING) ?: 0
        val totalPage = if (totalElements % size > 0) totalElements / size + 1 else totalElements / size
        val result = getRankDetails(keyWithScores)

        return RankListDto(
            size = size,
            totalPage = totalPage,
            currentPage = page,
            numberOfElements = result.size.toLong(),
            contents = result,
        )
    }

    private fun getRankDetails(keyWithScores: Set<TypedTuple<String>>): List<RankListDto.RankDetail> {
        return keyWithScores.fold(emptyList()) { acc, value ->
            val score = value.score!!
            val keys = value.value!!.split('@')
            val id = UUID.fromString(keys[0])
            val username = keys[1]

            acc + if (acc.isEmpty() || acc.last().score != score) {
                val key = redisTemplate.opsForZSet().reverseRangeByScore(RANKING, score, score, 0, 1)!!.first()
                RankListDto.RankDetail(
                    id,
                    username,
                    redisTemplate.opsForZSet().reverseRank(RANKING, key)!!.plus(1),
                    score,
                )
            } else {
                RankListDto.RankDetail(
                    id,
                    username,
                    acc.last().rank,
                    score,
                )
            }
        }
    }
}
