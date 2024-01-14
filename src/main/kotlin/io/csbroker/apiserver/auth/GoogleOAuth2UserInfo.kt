package io.csbroker.apiserver.auth

class GoogleOAuth2UserInfo(
    attributes: Map<String, Any>,
) : OAuth2UserInfo(
    id = attributes["sub"] as String,
    name = attributes["name"] as String,
    email = attributes["email"] as String,
    imageUrl = attributes["picture"] as String,
)
