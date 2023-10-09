package io.csbroker.apiserver.auth

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.user.UserRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class LoginUserArgumentResolver(
    private val userRepository: UserRepository
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        parameter.getParameterAnnotation(LoginUser::class.java) ?: return false
        return parameter.parameterType.isAssignableFrom(User::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication == null || authentication.principal.equals("anonymousUser")) {
            throw UnAuthorizedException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.")
        }
        val email = authentication.name

        return userRepository.findByEmail(email)
            ?: throw UnAuthorizedException(ErrorCode.NOT_FOUND_ENTITY, "알 수 없는 유저의 요청입니다.")
    }
}
