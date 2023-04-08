package io.csbroker.apiserver.service.common

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.Body
import aws.sdk.kotlin.services.ses.model.Content
import aws.sdk.kotlin.services.ses.model.Destination
import aws.sdk.kotlin.services.ses.model.Message
import aws.sdk.kotlin.services.ses.model.SendEmailRequest
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.repository.common.RedisRepository
import io.csbroker.apiserver.repository.user.UserRepository
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
    private val redisRepository: RedisRepository,

    @Value("\${aws.mail-url}")
    private val url: String,

    @Value("\${aws.access-key}")
    private val accessKey: String,

    @Value("\${aws.secret-key}")
    private val secretKey: String,
) : MailService {

    override suspend fun sendPasswordChangeMail(to: String) {
        checkUserExist(to)
        val code = UUID.randomUUID().toString()
        val emailRequest = createEmailRequest(code, to)
        send(emailRequest)
        redisRepository.setPasswordVerification(code, to)
    }

    private suspend fun checkUserExist(to: String) {
        val isExist = withContext(Dispatchers.IO) {
            userRepository.existsUserByEmail(to)
        }

        if (!isExist) {
            throw EntityNotFoundException("$to 메일로 가입한 유저를 찾을 수 없습니다.")
        }
    }

    private suspend fun send(emailRequest: SendEmailRequest) {
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

    private fun createEmailRequest(code: String, to: String): SendEmailRequest {
        return SendEmailRequest {
            destination = Destination {
                toAddresses = listOf(to)
            }
            message = Message {
                subject = Content {
                    data = CHANGE_EMAIL_TITLE
                }
                body = Body {
                    html = Content {
                        data = getEmailTemplate(code)
                    }
                }
            }
            source = NO_REPLY_SOURCE_EMAIL
        }
    }

    private fun getEmailTemplate(code: String): String? {
        val context = Context()
        context.setVariable("url", "https://$url/password-change/$code")
        return templateEngine.process("password-change", context)
    }

    companion object {
        private const val CHANGE_EMAIL_TITLE = "CS Broker 비밀번호 변경"
        private const val NO_REPLY_SOURCE_EMAIL = "no-reply@csbroker.io"
    }
}
