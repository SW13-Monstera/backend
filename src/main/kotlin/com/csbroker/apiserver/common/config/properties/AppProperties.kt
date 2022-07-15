package com.csbroker.apiserver.common.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val auth: Auth,
    val oAuth2: OAuth2
) {
    data class Auth(
        val tokenSecret: String,
        val tokenExpiry: Long,
        val refreshTokenExpiry: Long
    )

    data class OAuth2(
        var authorizedRedirectUris: List<String>
    ) {
        fun authorizedRedirectUris(authorizedRedirectUris: List<String>): OAuth2 {
            this.authorizedRedirectUris = authorizedRedirectUris
            return this
        }
    }
}
