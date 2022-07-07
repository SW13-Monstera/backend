package com.csbroker.apiserver.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.httpBasic().disable()
            .authorizeHttpRequests()
            .antMatchers("/**", "/").permitAll()
            .anyRequest().authenticated()
            .and().csrf()

        http.headers().frameOptions().sameOrigin()

        return http.build()
    }
}
