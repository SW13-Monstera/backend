package io.csbroker.apiserver.auth

import io.csbroker.apiserver.common.exception.OAuthProviderMissMatchException

class OAuth2UserInfoFactory {
    companion object {
        fun getOauth2UserInfo(providerType: ProviderType, attributes: MutableMap<String, Any>): OAuth2UserInfo {
            return when (providerType) {
                ProviderType.GOOGLE -> GoogleOAuth2UserInfo(attributes)
                ProviderType.GITHUB -> GithubOAuth2UserInfo(attributes)
                else -> throw OAuthProviderMissMatchException("프로바이더 타입이 일치하지 않습니다. ${providerType.name}")
            }
        }
    }
}
