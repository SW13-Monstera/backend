package com.csbroker.apiserver.service

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import com.csbroker.apiserver.repository.UserRepository
import com.csbroker.apiserver.repository.common.RedisRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.thymeleaf.spring5.SpringTemplateEngine
import java.util.UUID

@Service
class S3ServiceImpl(
    private val templateEngine: SpringTemplateEngine,
    private val userRepository: UserRepository,
    private val redisRepository: RedisRepository,

    @Value("\${spring.mail.url}")
    private val url: String,

    @Value("\${aws.access-key}")
    private val accessKey: String,

    @Value("\${aws.secret-key}")
    private val secretKey: String,

    @Value("\${aws.s3-bucket}")
    private val bucketName: String
) : S3Service {
    override suspend fun uploadProfileImg(multipartFile: MultipartFile): String {
        val s3FileName = "static/" + UUID.randomUUID().toString() + "-" + multipartFile.originalFilename

        val request = PutObjectRequest {
            bucket = bucketName
            key = s3FileName
            body = ByteStream.fromBytes(multipartFile.bytes)
        }

        S3Client {
            region = "ap-northeast-2"
            credentialsProvider = StaticCredentialsProvider {
                accessKeyId = accessKey
                secretAccessKey = secretKey
            }
        }.use {
            it.putObject(request)
        }

        return "https://$bucketName.s3.ap-northeast-2.amazonaws.com/$s3FileName"
    }
}
