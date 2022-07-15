package com.csbroker.apiserver.common.auth

class OAuth2UserInfoFactory {
    companion object {
        fun getOauth2UserInfo(providerType: ProviderType, attributes: MutableMap<String, Any>): OAuth2UserInfo {
            return when (providerType) {
                ProviderType.GOOGLE -> GoogleOAuth2UserInfo(attributes)
                ProviderType.GITHUB -> GithubOAuth2UserInfo(attributes)
                else -> throw IllegalArgumentException("Invalid Provider Type ${providerType.name}")
            }
        }
    }
}
