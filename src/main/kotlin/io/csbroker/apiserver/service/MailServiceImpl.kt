package io.csbroker.apiserver.service

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.repository.UserRepository
import io.csbroker.apiserver.repository.common.RedisRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.thymeleaf.context.Context
import org.thymeleaf.spring5.SpringTemplateEngine
import java.util.UUID

class MailServiceImpl(
    private val redisRepository: RedisRepository,
    private val templateEngine: SpringTemplateEngine,
    private val javaMailSender: JavaMailSender,
    private val userRepository: UserRepository,

    @Value("\${spring.mail.url}")
    private val url: String
) : MailService {

    override suspend fun sendPasswordChangeMail(to: String) {
        val isExist = userRepository.existsUserByEmail(to)

        if (!isExist) {
            throw EntityNotFoundException("$to 메일로 가입한 유저를 찾을 수 없습니다.")
        }

        val code = UUID.randomUUID().toString()
        val subject = "CS Broker 비밀번호 변경"

        val context = Context()
        context.setVariable("url", "https://$url/password-change/$code")

        val html = templateEngine.process("password-change", context)
        val message = javaMailSender.createMimeMessage()
        val messageHelper = MimeMessageHelper(message, true)

        messageHelper.setSubject(subject)
        messageHelper.setTo(to)
        messageHelper.setText(html, true)

        javaMailSender.send(message)
        redisRepository.setPasswordVerification(code, to)
    }
}
