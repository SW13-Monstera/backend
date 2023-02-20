package io.csbroker.apiserver.repository.common

import io.csbroker.apiserver.common.util.addCookie
import io.csbroker.apiserver.common.util.deleteCookie
import io.csbroker.apiserver.common.util.deserialize
import io.csbroker.apiserver.common.util.getCookie
import io.csbroker.apiserver.common.util.serialize
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME: String = "oauth2_auth_request"
const val REDIRECT_URI_PARAM_COOKIE_NAME: String = "redirect_uri"
const val REFRESH_TOKEN: String = "refresh_token"
private const val COOKIE_EXPIRE_SECONDS = 180L

class OAuth2AuthorizationRequestBasedOnCookieRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val cookie = getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME) ?: return null

        return deserialize(cookie, OAuth2AuthorizationRequest::class.java)
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        if (authorizationRequest == null) {
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
            deleteCookie(request, response, REFRESH_TOKEN)
            return
        }

        addCookie(
            response,
            OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
            serialize(authorizationRequest),
            COOKIE_EXPIRE_SECONDS,
        )

        val redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME)
        if (redirectUriAfterLogin.isNotBlank()) {
            addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS)
        }
    }

    @Deprecated(
        message = "deprecated at original interface",
        replaceWith = ReplaceWith("this.loadAuthorizationRequest(request)"),
    )
    override fun removeAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return loadAuthorizationRequest(request)
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): OAuth2AuthorizationRequest? {
        return loadAuthorizationRequest(request)
    }

    fun removeAuthorizationRequestCookies(request: HttpServletRequest, response: HttpServletResponse) {
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
        deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
        deleteCookie(request, response, REFRESH_TOKEN)
    }
}
