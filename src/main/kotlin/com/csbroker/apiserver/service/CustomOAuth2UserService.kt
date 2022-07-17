package com.csbroker.apiserver.service

import com.csbroker.apiserver.common.auth.OAuth2UserInfo
import com.csbroker.apiserver.common.auth.OAuth2UserInfoFactory
import com.csbroker.apiserver.common.auth.ProviderType
import com.csbroker.apiserver.common.auth.UserPrincipal
import com.csbroker.apiserver.model.User
import com.csbroker.apiserver.repository.UserRepository
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository
) : DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val user = super.loadUser(userRequest)

        try {
            return this.process(userRequest!!, user)
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw InternalAuthenticationServiceException(e.message)
        }
    }

    private fun process(userRequest: OAuth2UserRequest, user: OAuth2User): OAuth2User {
        val providerType =
            ProviderType.valueOf(userRequest.clientRegistration.registrationId.uppercase(Locale.getDefault()))
        val userInfo = OAuth2UserInfoFactory.getOauth2UserInfo(providerType, user.attributes)
        var savedUser = userRepository.findByEmail(userInfo.getEmail())

        if(savedUser != null){
            if (providerType != savedUser.providerType) {
                throw IllegalArgumentException(
                    "This user signed up with ${savedUser.providerType} account not ${providerType.name}"
                )
            }
            this.updateUser(savedUser, userInfo)
        } else {
            savedUser = this.createUser(userInfo, providerType)
        }

        return UserPrincipal.create(savedUser, user.attributes)
    }

    private fun createUser(userInfo: OAuth2UserInfo, providerType: ProviderType): User {
        val user = User(
            email = userInfo.getEmail(),
            username = userInfo.getName(),
            providerType = providerType,
            profileImageUrl = userInfo.getImageUrl()
        )

        return userRepository.saveAndFlush(user)
    }

    private fun updateUser(user: User, userInfo: OAuth2UserInfo): User {
        user.updateInfo(userInfo)
        return user
    }
}
