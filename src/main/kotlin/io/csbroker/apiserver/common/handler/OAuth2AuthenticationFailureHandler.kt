package io.csbroker.apiserver.common.handler

import io.csbroker.apiserver.common.util.getCookie
import io.csbroker.apiserver.repository.common.OAuth2AuthorizationRequestBasedOnCookieRepository
import io.csbroker.apiserver.repository.common.REDIRECT_URI_PARAM_COOKIE_NAME
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationFailureHandler(
    private val authorizationRequestRepository: OAuth2AuthorizationRequestBasedOnCookieRepository,
) : SimpleUrlAuthenticationFailureHandler() {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        var targetUrl = getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)?.value ?: "/"

        exception.printStackTrace()

        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
            .queryParam("error", exception.localizedMessage)
            .build().toUriString()

        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response)

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
