package com.csbroker.apiserver.repository.common

import aws.smithy.kotlin.runtime.util.push
import com.csbroker.apiserver.common.config.properties.AppProperties
import com.csbroker.apiserver.dto.common.RankListDto
import com.csbroker.apiserver.dto.user.RankResultDto
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations.TypedTuple
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.TimeUnit

const val PASSWORD_VERIFICATION_MINUTE = 5

@Repository
class RedisRepository(
    private val redisTemplate: StringRedisTemplate,
    private val appProperties: AppProperties
) {
    fun getRefreshTokenByEmail(email: String): String? {
        return redisTemplate.opsForValue().get(email)
    }

    fun setRefreshTokenByEmail(email: String, refreshToken: String) {
        redisTemplate.opsForValue()
            .set(email, refreshToken, appProperties.auth.refreshTokenExpiry, TimeUnit.MILLISECONDS)
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

    fun setRank(scoreMap: Map<String, Double>) {
        scoreMap.forEach { (t, u) ->
            println(t)
            println(u)
        }
        redisTemplate.opsForZSet().add(
            "ranking",
            scoreMap.map { TypedTuple.of(it.key, it.value) }.toSet()
        )
    }

    fun getRank(key: String): RankResultDto {
        var rank: Long? = null

        val score = redisTemplate.opsForZSet().score("ranking", key) ?: 0.0
        val rankKey = redisTemplate.opsForZSet().reverseRangeByScore("ranking", score, score, 0, 1)?.first()

        if (rankKey != null) {
            rank = redisTemplate.opsForZSet().reverseRank("ranking", rankKey)?.plus(1)
        }

        return RankResultDto(rank, score)
    }

    fun getRanks(size: Long, page: Long): RankListDto {
        val start = size * page
        val end = size * (page + 1) - 1

        val keyWithScores = redisTemplate.opsForZSet().reverseRangeWithScores("ranking", start, end)
        val totalElements = redisTemplate.opsForZSet().size("ranking") ?: 0
        val totalPage = if (totalElements % size > 0) totalElements / size + 1 else totalElements / size
        val result = mutableListOf<RankListDto.RankDetail>()
        var rank = 1L
        var cnt = 0

        if (keyWithScores != null) {
            for ((index, keyWithScore) in keyWithScores.withIndex()) {
                if (index == 0) {
                    val score = keyWithScore.score!!
                    val key = redisTemplate.opsForZSet().reverseRangeByScore("ranking", score, score, 0, 1)!!.first()
                    rank = redisTemplate.opsForZSet().reverseRank("ranking", key)!!.plus(1)
                } else {
                    if (result.last().score == keyWithScore.score) {
                        cnt += 1
                    } else {
                        rank += cnt
                        cnt = 1
                    }
                }

                val keys = keyWithScore.value!!.split('@')

                val id = UUID.fromString(keys[0])
                val username = keys[1]

                result.push(
                    RankListDto.RankDetail(
                        id,
                        username,
                        rank,
                        keyWithScore.score!!
                    )
                )
            }
        }

        return RankListDto(
            size = size,
            totalPage = totalPage,
            currentPage = page,
            numberOfElements = result.size.toLong(),
            contents = result
        )
    }
}
