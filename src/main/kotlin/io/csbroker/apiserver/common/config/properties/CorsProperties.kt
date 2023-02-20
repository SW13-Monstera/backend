package io.csbroker.apiserver.common.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "cors")
@ConstructorBinding
data class CorsProperties(
    val allowedOrigins: String,
    val allowedMethods: String,
    val allowedHeaders: String,
    val maxAge: Long,
)
