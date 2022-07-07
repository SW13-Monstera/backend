package com.csbroker.apiserver.common.auth

import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.Collections

class UserPrincipal(
    private val userId: String,
    private val password: String,
    private val providerType: ProviderType,
    private val roleType: Role,
    private val authorities: MutableCollection<GrantedAuthority>
) : OAuth2User, UserDetails, OidcUser {

    private lateinit var attributes: MutableMap<String, Any>

    override fun getName(): String {
        return this.userId
    }

    override fun getAttributes(): MutableMap<String, Any> {
        return this.attributes
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return this.authorities
    }

    override fun getPassword(): String {
        return this.password
    }

    override fun getUsername(): String {
        return this.userId
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getClaims(): MutableMap<String, Any>? {
        return null
    }

    override fun getUserInfo(): OidcUserInfo? {
        return null
    }

    override fun getIdToken(): OidcIdToken? {
        return null
    }

    companion object {
        fun create(user: User): UserPrincipal {
            return UserPrincipal(
                user.id.toString(),
                user.password,
                user.providerType,
                Role.ROLE_USER,
                Collections.singletonList(SimpleGrantedAuthority(Role.ROLE_USER.name))
            )
        }

        fun create(user: User, attributes: MutableMap<String, Any>): UserPrincipal {
            val userPrincipal = create(user)
            userPrincipal.attributes = attributes

            return userPrincipal
        }
    }
}
