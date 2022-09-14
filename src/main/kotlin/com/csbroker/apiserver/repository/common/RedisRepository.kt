package com.csbroker.apiserver.repository.common

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class RedisRepository(
    private val redisTemplate: StringRedisTemplate
) {
    fun getRefreshTokenByEmail(email: String): String? {
        return redisTemplate.opsForValue().get(email)
    }

    fun setRefreshTokenByEmail(email: String, refreshToken: String) {
        redisTemplate.opsForValue().set(email, refreshToken)
    }

    fun setPasswordVerification(code: String, email: String) {
        redisTemplate.opsForValue().set(code, email, 5, TimeUnit.MINUTES)
    }

    fun getEmailByCode(code: String): String? {
        return redisTemplate.opsForValue().get(code)
    }
}
