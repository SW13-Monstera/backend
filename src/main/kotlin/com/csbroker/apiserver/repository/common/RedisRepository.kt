package com.csbroker.apiserver.repository.common

import com.csbroker.apiserver.common.config.properties.AppProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
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
}
