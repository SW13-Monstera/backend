package io.csbroker.apiserver.auth

import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.model.User
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
    private val authorities: Collection<GrantedAuthority>,
) : OAuth2User, UserDetails, OidcUser {

    private lateinit var attributes: Map<String, Any>

    override fun getName(): String {
        return userId
    }

    override fun getAttributes(): Map<String, Any> {
        return attributes
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return userId
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

    override fun getClaims(): Map<String, Any>? {
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
                Collections.singletonList(SimpleGrantedAuthority(Role.ROLE_USER.name)),
            )
        }

        fun create(user: User, attributes: Map<String, Any>): UserPrincipal {
            val userPrincipal = create(user)
            userPrincipal.attributes = attributes

            return userPrincipal
        }
    }
}
