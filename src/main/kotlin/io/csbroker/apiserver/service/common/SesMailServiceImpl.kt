package io.csbroker.apiserver.service.common

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.Body
import aws.sdk.kotlin.services.ses.model.Content
import aws.sdk.kotlin.services.ses.model.Destination
import aws.sdk.kotlin.services.ses.model.Message
import aws.sdk.kotlin.services.ses.model.SendEmailRequest
import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.repository.common.RedisRepository
import io.csbroker.apiserver.repository.user.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.util.UUID

@Service
class SesMailServiceImpl(
    private val templateEngine: SpringTemplateEngine,
    private val userRepository: UserRepository,
    private val redisRepository: RedisRepository,
    @Value("\${aws.mail-url}")
    private val url: String,
    private val sesClient: SesClient,
) : MailService {

    override suspend fun sendPasswordChangeMail(email: String) {
        checkUserExist(email)
        val code = UUID.randomUUID().toString()
        val emailRequest = createEmailRequest(code, email)
        sesClient.sendEmail(emailRequest)
        redisRepository.setPasswordVerification(code, email)
    }

    private fun checkUserExist(email: String) {
        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 메일로 가입한 유저를 찾을 수 없습니다.")

        if (user.providerType != ProviderType.LOCAL) {
            throw EntityNotFoundException("소셜 로그인 유저는 비밀번호를 변경할 수 없습니다.")
        }
    }

    private fun createEmailRequest(code: String, email: String): SendEmailRequest {
        return SendEmailRequest {
            destination = Destination {
                toAddresses = listOf(email)
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
