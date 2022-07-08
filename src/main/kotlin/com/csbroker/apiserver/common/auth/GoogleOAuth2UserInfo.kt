package com.csbroker.apiserver.common.auth

class GoogleOAuth2UserInfo(
    attributes: MutableMap<String, Any>
) : OAuth2UserInfo(attributes) {

    override fun getId(): String {
        TODO("Not yet implemented")
    }

    override fun getName(): String {
        TODO("Not yet implemented")
    }

    override fun getEmail(): String {
        TODO("Not yet implemented")
    }

    override fun getImageUrl(): String {
        TODO("Not yet implemented")
    }
}
