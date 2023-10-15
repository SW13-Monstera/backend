package io.csbroker.apiserver.auth

abstract class OAuth2UserInfo(
    val id: String,
    val name: String,
    val email: String,
    val imageUrl: String,
)
