package io.csbroker.apiserver.service

import io.csbroker.apiserver.auth.UserPrincipal
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            ?: throw EntityNotFoundException("$username 을 가진 유저는 존재하지 않습니다.")

        return UserPrincipal.create(user)
    }
}
