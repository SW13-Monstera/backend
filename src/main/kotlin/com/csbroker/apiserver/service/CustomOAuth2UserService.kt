package com.csbroker.apiserver.service

import com.csbroker.apiserver.common.auth.OAuth2UserInfo
import com.csbroker.apiserver.common.auth.OAuth2UserInfoFactory
import com.csbroker.apiserver.common.auth.ProviderType
import com.csbroker.apiserver.common.auth.UserPrincipal
import com.csbroker.apiserver.common.enums.ErrorCode
import com.csbroker.apiserver.common.exception.InternalServiceException
import com.csbroker.apiserver.common.exception.OAuthProviderMissMatchException
import com.csbroker.apiserver.common.util.GithubClient
import com.csbroker.apiserver.model.User
import com.csbroker.apiserver.repository.UserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val githubClient: GithubClient
) : DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val user = super.loadUser(userRequest)

        try {
            return this.process(userRequest!!, user)
        } catch (e: OAuthProviderMissMatchException) {
            throw e
        } catch (e: Exception) {
            throw InternalServiceException(ErrorCode.SERVER_ERROR, e.message.toString())
        }
    }

    private fun process(userRequest: OAuth2UserRequest, user: OAuth2User): OAuth2User {
        val providerType =
            ProviderType.valueOf(userRequest.clientRegistration.registrationId.uppercase(Locale.getDefault()))

        val accessToken = userRequest.accessToken.tokenValue

        val attributes = user.attributes.toMutableMap()

        if (providerType == ProviderType.GITHUB && attributes["email"] == null) {
            val emailResponseDto = githubClient.getUserEmail("Bearer $accessToken").first { it.primary }
            attributes["email"] = emailResponseDto.email
        }

        val userInfo = OAuth2UserInfoFactory.getOauth2UserInfo(providerType, attributes)
        var savedUser = this.userRepository.findByEmail(userInfo.getEmail())
            ?: this.userRepository.findUserByProviderId(userInfo.getId())

        if (savedUser != null) {
            if (providerType != savedUser.providerType) {
                throw OAuthProviderMissMatchException(
                    "${attributes["email"]} 유저는 ${savedUser.providerType} 로 가입하였습니다."
                )
            }
        } else {
            savedUser = this.createUser(userInfo, providerType)
        }

        return UserPrincipal.create(savedUser, attributes)
    }

    private fun createUser(userInfo: OAuth2UserInfo, providerType: ProviderType): User {
        val user = User(
            email = userInfo.getEmail(),
            username = userInfo.getName(),
            providerType = providerType,
            profileImageUrl = userInfo.getImageUrl(),
            providerId = userInfo.getId()
        )

        return userRepository.saveAndFlush(user)
    }
}
