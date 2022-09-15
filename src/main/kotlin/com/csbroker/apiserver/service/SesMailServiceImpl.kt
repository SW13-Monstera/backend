package com.csbroker.apiserver.service

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.Body
import aws.sdk.kotlin.services.ses.model.Content
import aws.sdk.kotlin.services.ses.model.Destination
import aws.sdk.kotlin.services.ses.model.Message
import aws.sdk.kotlin.services.ses.model.SendEmailRequest
import com.csbroker.apiserver.common.exception.EntityNotFoundException
import com.csbroker.apiserver.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring5.SpringTemplateEngine
import java.util.UUID

@Service
class SesMailServiceImpl(
    private val templateEngine: SpringTemplateEngine,
    private val userRepository: UserRepository,

    @Value("\${spring.mail.url}")
    private val url: String,

    @Value("\${aws.access-key}")
    private val accessKey: String,

    @Value("\${aws.secret-key}")
    private val secretKey: String
) : MailService {

    override suspend fun sendPasswordChangeMail(to: String) {
        val isExist = withContext(Dispatchers.IO) {
            userRepository.existsUserByEmail(to)
        }

        if (!isExist) {
            throw EntityNotFoundException("$to 메일로 가입한 유저를 찾을 수 없습니다.")
        }

        val code = UUID.randomUUID().toString()
        val context = Context()
        context.setVariable("url", "https://$url/password-change/$code")
        val htmlTemplate = templateEngine.process("password-change", context)

        val emailRequest = SendEmailRequest {
            destination = Destination {
                toAddresses = listOf(to)
            }
            message = Message {
                subject = Content {
                    data = "CS Broker 비밀번호 변경"
                }
                body = Body {
                    html = Content {
                        data = htmlTemplate
                    }
                }
            }
            source = "no-reply@csbroker.io"
        }

        SesClient {
            region = "ap-northeast-2"
            credentialsProvider = StaticCredentialsProvider {
                accessKeyId = accessKey
                secretAccessKey = secretKey
            }
        }.use {
            it.sendEmail(emailRequest)
        }
    }
}
