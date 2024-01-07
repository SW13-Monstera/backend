package io.csbroker.apiserver.service.auth

import io.csbroker.apiserver.auth.OAuth2UserInfo
import io.csbroker.apiserver.auth.OAuth2UserInfoFactory
import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.auth.UserPrincipal
import io.csbroker.apiserver.common.client.GithubClient
import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.InternalServiceException
import io.csbroker.apiserver.common.exception.OAuthProviderMissMatchException
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.user.UserRepository
import io.sentry.Sentry
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val githubClient: GithubClient,
) : DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val user = super.loadUser(userRequest)

        return runCatching {
            process(userRequest!!, user)
        }.onFailure {
            Sentry.captureException(it)
            if (it is OAuthProviderMissMatchException) {
                throw it
            }
            throw InternalServiceException(ErrorCode.SERVER_ERROR, it.message.toString())
        }.getOrThrow()
    }

    private fun process(userRequest: OAuth2UserRequest, user: OAuth2User): OAuth2User {
        val providerType =
            ProviderType.valueOf(userRequest.clientRegistration.registrationId.uppercase(Locale.getDefault()))

        val accessToken = userRequest.accessToken.tokenValue

        val attributes = if (providerType == ProviderType.GITHUB && user.attributes["email"] == null) {
            setGithubPrimaryEmail(accessToken, user.attributes)
        } else {
            user.attributes
        }

        return UserPrincipal.create(getOrCreateUser(providerType, attributes), attributes)
    }

    private fun setGithubPrimaryEmail(
        accessToken: String?,
        attributes: Map<String, Any>,
    ): Map<String, Any> {
        val emailResponseDto = githubClient.getUserEmail("Bearer $accessToken").first { it.primary }
        return hashMapOf<String, Any>().also {
            it.putAll(attributes)
            it["email"] = emailResponseDto.email
        }
    }

    private fun getOrCreateUser(
        providerType: ProviderType,
        attributes: Map<String, Any>,
    ): User {
        val userInfo = OAuth2UserInfoFactory.getOauth2UserInfo(providerType, attributes)
        val savedUser = userRepository.findByEmail(userInfo.email)
            ?: userRepository.findUserByProviderId(userInfo.id)

        return savedUser ?: createUser(userInfo, providerType)
    }

    private fun createUser(userInfo: OAuth2UserInfo, providerType: ProviderType): User {
        val user = User(
            email = userInfo.email,
            username = userInfo.name,
            providerType = providerType,
            profileImageUrl = userInfo.imageUrl,
            providerId = userInfo.id,
        )

        return userRepository.saveAndFlush(user)
    }
}
