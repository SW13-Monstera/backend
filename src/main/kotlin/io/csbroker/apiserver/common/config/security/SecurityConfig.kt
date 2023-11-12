package io.csbroker.apiserver.common.config.security

import io.csbroker.apiserver.auth.AuthTokenProvider
import io.csbroker.apiserver.common.config.properties.AppProperties
import io.csbroker.apiserver.common.config.properties.CorsProperties
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.common.exception.RestAuthenticationEntryPoint
import io.csbroker.apiserver.common.filter.TokenAuthenticationFilter
import io.csbroker.apiserver.common.handler.OAuth2AuthenticationFailureHandler
import io.csbroker.apiserver.common.handler.OAuth2AuthenticationSuccessHandler
import io.csbroker.apiserver.common.handler.TokenAccessDeniedHandler
import io.csbroker.apiserver.repository.common.OAuth2AuthorizationRequestBasedOnCookieRepository
import io.csbroker.apiserver.repository.common.RedisRepository
import io.csbroker.apiserver.repository.user.UserRepository
import io.csbroker.apiserver.service.auth.CustomOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsUtils
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
)
class SecurityConfig(
    private val corsProperties: CorsProperties,
    private val appProperties: AppProperties,
    private val authTokenProvider: AuthTokenProvider,
    private val oAuth2UserService: CustomOAuth2UserService,
    private val redisRepository: RedisRepository,
    private val tokenAccessDeniedHandler: TokenAccessDeniedHandler,
    private val userRepository: UserRepository,
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
            .requestMatchers(
                AntPathRequestMatcher("/"),
                AntPathRequestMatcher("/error"),
                AntPathRequestMatcher("/favicon.ico"),
                AntPathRequestMatcher("/**/*.png"),
                AntPathRequestMatcher("/**/*.gif"),
                AntPathRequestMatcher("/**/*.svg"),
                AntPathRequestMatcher("/**/*.jpg"),
                AntPathRequestMatcher("/**/*.html"),
                AntPathRequestMatcher("/**/*.css"),
                AntPathRequestMatcher("/**/*.js"),
            )
            .permitAll()
            .requestMatchers(AntPathRequestMatcher("/api/v1/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher("/api/v2/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher("/actuator/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher("/api/admin/**")).hasAuthority(Role.ROLE_ADMIN.code)
            .anyRequest().authenticated()
            .and()
            .oauth2Login()
            .authorizationEndpoint()
            .baseUri("/oauth2/authorization")
            .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
            .and()
            .redirectionEndpoint()
            .baseUri("/*/oauth2/code/*")
            .and()
            .userInfoEndpoint()
            .userService(oAuth2UserService)
            .and()
            .successHandler(oAuth2AuthenticationSuccessHandler())
            .failureHandler(oAuth2AuthenticationFailureHandler())

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
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
            redisRepository,
            userRepository,
        )
    }

    @Bean
    fun oAuth2AuthenticationFailureHandler(): OAuth2AuthenticationFailureHandler {
        return OAuth2AuthenticationFailureHandler(
            oAuth2AuthorizationRequestBasedOnCookieRepository(),
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
