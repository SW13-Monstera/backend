package com.csbroker.apiserver.common.handler

import com.csbroker.apiserver.common.auth.AuthTokenProvider
import com.csbroker.apiserver.common.auth.OAuth2UserInfoFactory
import com.csbroker.apiserver.common.auth.ProviderType
import com.csbroker.apiserver.common.config.properties.AppProperties
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.common.util.addCookie
import com.csbroker.apiserver.common.util.deleteCookie
import com.csbroker.apiserver.common.util.getCookie
import com.csbroker.apiserver.repository.OAuth2AuthorizationRequestBasedOnCookieRepository
import com.csbroker.apiserver.repository.REDIRECT_URI_PARAM_COOKIE_NAME
import com.csbroker.apiserver.repository.REFRESH_TOKEN
import com.csbroker.apiserver.repository.RedisRepository
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.Date
import java.util.Locale
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class OAuth2AuthenticationSuccessHandler(
    private val appProperties: AppProperties,
    private val authorizationRequestRepository: OAuth2AuthorizationRequestBasedOnCookieRepository,
    private val tokenProvider: AuthTokenProvider,
    private val redisRepository: RedisRepository
) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val targetUrl = determineTargetUrl(request, response, authentication)

        if (response.isCommitted) {
            logger.debug("Response has already been committed. Unable to redirect to $targetUrl")
            return
        }

        this.clearAuthenticationAttributes(request, response)
        this.redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): String {
        val redirectUri = getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)?.value

        if (redirectUri != null && !this.isAuthorizedRedirectUri(redirectUri)) {
            throw IllegalArgumentException(
                "Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication"
            )
        }

        val targetUrl = redirectUri ?: defaultTargetUrl

        val authToken = authentication as OAuth2AuthenticationToken
        val providerType = ProviderType.valueOf(authToken.authorizedClientRegistrationId.uppercase(Locale.getDefault()))

        val user = authentication.principal as OidcUser
        val userInfo = OAuth2UserInfoFactory.getOauth2UserInfo(providerType, user.attributes)
        val authorities = user.authorities

        val roleType = if (this.hasAuthority(authorities, Role.ROLE_ADMIN.code)) Role.ROLE_ADMIN else Role.ROLE_USER

        val now = Date()
        val tokenExpiry = appProperties.auth.tokenExpiry
        val refreshTokenExpiry = appProperties.auth.refreshTokenExpiry

        val accessToken = tokenProvider.createAuthToken(
            userInfo.getEmail(),
            Date(now.time + tokenExpiry),
            roleType.code
        )

        val refreshToken = tokenProvider.createAuthToken(
            appProperties.auth.tokenSecret,
            Date(now.time + refreshTokenExpiry)
        )

        redisRepository.setRefreshTokenByEmail(userInfo.getEmail(), refreshToken.token)

        val cookieMaxAge = (refreshTokenExpiry / 60).toInt()

        deleteCookie(request, response, REFRESH_TOKEN)
        addCookie(response, REFRESH_TOKEN, refreshToken.token, cookieMaxAge)

        return UriComponentsBuilder.fromUriString(targetUrl)
            .queryParam("token", accessToken.token)
            .build().toUriString()
    }

    fun clearAuthenticationAttributes(request: HttpServletRequest, response: HttpServletResponse) {
        super.clearAuthenticationAttributes(request)
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
    }

    private fun hasAuthority(authorities: Collection<GrantedAuthority>?, authority: String): Boolean {
        if (authorities == null) {
            return false
        }

        for (grantedAuthority in authorities) {
            if (authority == grantedAuthority.authority) {
                return true
            }
        }
        return false
    }

    private fun isAuthorizedRedirectUri(uri: String): Boolean {
        val clientRedirectUri: URI = URI.create(uri)
        return appProperties.oAuth2.authorizedRedirectUris
            .stream()
            .anyMatch { authorizedRedirectUri ->
                val authorizedURI: URI = URI.create(authorizedRedirectUri)
                authorizedURI.host.equals(clientRedirectUri.host, ignoreCase = true) &&
                    authorizedURI.port == clientRedirectUri.port
            }
    }
}
