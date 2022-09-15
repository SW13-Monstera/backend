package com.csbroker.apiserver.common.config

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.ses.SesClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SesConfig(
    @Value("\${aws.access-key}")
    private val accessKey: String,

    @Value("\${aws.secret-key}")
    private val secretKey: String
) {

    @Bean
    fun sesClient(): SesClient {
        return SesClient {
            region = "ap-northeast-2"
            credentialsProvider = StaticCredentialsProvider {
                accessKeyId = accessKey
                secretAccessKey = secretKey
            }
        }
    }
}
