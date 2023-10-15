package io.csbroker.apiserver.auth

class GithubOAuth2UserInfo(
    attributes: MutableMap<String, Any>,
) : OAuth2UserInfo(
    id = (attributes["id"] as Int).toString(),
    name = attributes["login"] as String,
    email = attributes["email"] as String,
    imageUrl = attributes["avatar_url"] as String,
)
