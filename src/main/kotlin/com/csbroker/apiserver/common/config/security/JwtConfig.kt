package com.csbroker.apiserver.common.config.security

import com.csbroker.apiserver.common.auth.AuthTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JwtConfig(
    @Value("\${jwt.secret}")
    private val secret: String
) {
    @Bean
    fun jwtTokenProvider(): AuthTokenProvider {
        return AuthTokenProvider(secret)
    }
}
