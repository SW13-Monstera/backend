package com.csbroker.apiserver.common.config.security

import com.csbroker.apiserver.common.auth.AuthTokenProvider
import com.csbroker.apiserver.common.config.properties.AppProperties
import com.csbroker.apiserver.common.config.properties.CorsProperties
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.common.exception.RestAuthenticationEntryPoint
import com.csbroker.apiserver.common.filter.TokenAuthenticationFilter
import com.csbroker.apiserver.common.handler.OAuth2AuthenticationFailureHandler
import com.csbroker.apiserver.common.handler.OAuth2AuthenticationSuccessHandler
import com.csbroker.apiserver.common.handler.TokenAccessDeniedHandler
import com.csbroker.apiserver.repository.OAuth2AuthorizationRequestBasedOnCookieRepository
import com.csbroker.apiserver.repository.RedisRepository
import com.csbroker.apiserver.service.CustomOAuth2UserService
import com.csbroker.apiserver.service.CustomUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsUtils
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true
)
class SecurityConfig(
    private val corsProperties: CorsProperties,
    private val appProperties: AppProperties,
    private val authTokenProvider: AuthTokenProvider,
    private val userDetailsService: CustomUserDetailsService,
    private val oAuth2UserService: CustomOAuth2UserService,
    private val redisRepository: RedisRepository,
    private val tokenAccessDeniedHandler: TokenAccessDeniedHandler
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .exceptionHandling()
            .authenticationEntryPoint(RestAuthenticationEntryPoint())
            .accessDeniedHandler(tokenAccessDeniedHandler)
            .and()
            .authorizeRequests()
            .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
            .antMatchers(
                "/",
                "/error",
                "/favicon.ico",
                "/**/*.png",
                "/**/*.gif",
                "/**/*.svg",
                "/**/*.jpg",
                "/**/*.html",
                "/**/*.css",
                "/**/*.js"
            )
            .permitAll()
            .antMatchers("/api/v1/**").permitAll()
            .antMatchers("/actuator/**").permitAll()
            .antMatchers("/api/admin/**").hasAuthority(Role.ROLE_ADMIN.code)
            .anyRequest().authenticated()
            .and()
            .oauth2Login()
            .authorizationEndpoint()
            .baseUri("/oauth2/authorization")
            .authorizationRequestRepository(this.oAuth2AuthorizationRequestBasedOnCookieRepository())
            .and()
            .redirectionEndpoint()
            .baseUri("/*/oauth2/code/*")
            .and()
            .userInfoEndpoint()
            .userService(oAuth2UserService)
            .and()
            .successHandler(this.oAuth2AuthenticationSuccessHandler())
            .failureHandler(this.oAuth2AuthenticationFailureHandler())

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun tokenAuthenticationFilter(): TokenAuthenticationFilter {
        return TokenAuthenticationFilter(authTokenProvider)
    }

    @Bean
    fun oAuth2AuthorizationRequestBasedOnCookieRepository(): OAuth2AuthorizationRequestBasedOnCookieRepository {
        return OAuth2AuthorizationRequestBasedOnCookieRepository()
    }

    @Bean
    fun oAuth2AuthenticationSuccessHandler(): OAuth2AuthenticationSuccessHandler {
        return OAuth2AuthenticationSuccessHandler(
            appProperties,
            oAuth2AuthorizationRequestBasedOnCookieRepository(),
            authTokenProvider,
            redisRepository
        )
    }

    @Bean
    fun oAuth2AuthenticationFailureHandler(): OAuth2AuthenticationFailureHandler {
        return OAuth2AuthenticationFailureHandler(
            oAuth2AuthorizationRequestBasedOnCookieRepository()
        )
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val corsConfigSource = UrlBasedCorsConfigurationSource()
        val corsConfig = CorsConfiguration()
        corsConfig.allowedHeaders = corsProperties.allowedHeaders.split(",")
        corsConfig.allowedMethods = corsProperties.allowedMethods.split(",")
        corsConfig.allowedOrigins = corsProperties.allowedOrigins.split(",")
        corsConfig.allowCredentials = true
        corsConfig.maxAge = corsProperties.maxAge
        corsConfigSource.registerCorsConfiguration("/**", corsConfig)
        return corsConfigSource
    }
}
