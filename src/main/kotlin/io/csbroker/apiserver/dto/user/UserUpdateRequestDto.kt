package io.csbroker.apiserver.dto.user

data class UserUpdateRequestDto(
    val profileImageUrl: String?,
    val username: String?,
    var password: String?,
    var major: String? = null,
    var job: String? = null,
    var jobObjective: String? = null,
    var techs: List<String>? = null,
    var githubUrl: String? = null,
    var linkedinUrl: String? = null
)