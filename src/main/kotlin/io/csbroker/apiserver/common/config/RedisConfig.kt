package io.csbroker.apiserver.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class RedisConfig(
    @Value("\${spring.redis.host}")
    private val host: String,

    @Value("\${spring.redis.port}")
    private val port: Int,
) {

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory(host, port)
    }

    @Bean
    fun stringRedisTemplate(): StringRedisTemplate {
        return StringRedisTemplate().also {
            it.keySerializer = StringRedisSerializer()
            it.valueSerializer = StringRedisSerializer()
            it.connectionFactory = redisConnectionFactory()
            it.setEnableTransactionSupport(true)
        }
    }

    @Bean
    fun transactionManager(): PlatformTransactionManager {
        return JpaTransactionManager()
    }
}
