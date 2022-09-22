package com.csbroker.apiserver.repository.common

import com.csbroker.apiserver.common.config.properties.AppProperties
import com.csbroker.apiserver.dto.user.RankResultDto
import org.springframework.data.redis.core.StringRedisTemplate
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

    fun setRank(scoreMap: Map<UUID, Double>) {
        scoreMap.forEach {
            redisTemplate.opsForZSet().add("ranking", it.key.toString(), it.value)
        }
    }

    fun getRank(userId: UUID): RankResultDto {
        var rank = 0L

        val score = redisTemplate.opsForZSet().score("ranking", userId.toString()) ?: 0.0
        val rankKeys = redisTemplate.opsForZSet().reverseRangeByScore("ranking", score, score, 0, 1)

        if (rankKeys != null) {
            for (rankKey in rankKeys) {
                rank = redisTemplate.opsForZSet().reverseRank("ranking", rankKey!!)!!
            }
        }

        return RankResultDto(rank + 1, score)
    }
}
