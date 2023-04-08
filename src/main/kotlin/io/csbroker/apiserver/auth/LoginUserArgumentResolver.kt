package io.csbroker.apiserver.auth

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import org.springframework.core.MethodParameter
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class LoginUserArgumentResolver(
    private val isTest: Boolean = false,
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
        val principal = if (isTest) {
            User("admin@csbroker.io", "1234", listOf(SimpleGrantedAuthority(Role.ROLE_ADMIN.code)))
        } else {
            SecurityContextHolder.getContext().authentication?.principal
        }

        // 유저 검증이 되지 않으면, anonymousUser 라는 String으로 넘어옴. 그것을 방지하기 위한 검증.
        if (principal is String) {
            throw UnAuthorizedException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.")
        }

        return principal ?: throw UnAuthorizedException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.")
    }
}
