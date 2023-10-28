package io.csbroker.apiserver.controller.v1.common

import io.csbroker.apiserver.common.config.properties.AppProperties
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.auth.PasswordChangeMailRequestDto
import io.csbroker.apiserver.dto.auth.PasswordChangeRequestDto
import io.csbroker.apiserver.dto.auth.TokenDto
import io.csbroker.apiserver.dto.user.UserInfoDto
import io.csbroker.apiserver.dto.user.UserLoginRequestDto
import io.csbroker.apiserver.dto.user.UserSignUpDto
import io.csbroker.apiserver.service.auth.AuthService
import io.csbroker.apiserver.service.common.MailService
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import io.restassured.http.Method
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import java.util.UUID

class AuthControllerTest : RestDocsTest() {
    private val AUTH_ENDPOINT = "/api/v1/auth"
    private lateinit var mockMvc: MockMvcRequestSpecification
    private lateinit var authService: AuthService
    private lateinit var mailService: MailService

    @BeforeEach
    fun setUp() {
        authService = mockk()
        mailService = mockk()
        mockMvc = mockMvc(
            AuthController(
                authService,
                AppProperties(
                    auth = AppProperties.Auth(
                        tokenExpiry = 1000,
                        tokenSecret = "secret",
                        refreshTokenExpiry = 1000,
                    ),
                    oAuth2 = AppProperties.OAuth2(
                        authorizedRedirectUris = listOf("http://localhost:8080"),
                    ),
                ),
                mailService,
            ),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `Signup v1 200 OK`() {
        // given
        every { authService.saveUser(any()) } returns UUID.randomUUID()
        val userSignUpDto = UserSignUpDto(
            username = "test",
            email = "test@test.com",
            password = "Test123@!",
        )

        // when
        val response = mockMvc.body(userSignUpDto).request(Method.POST, "$AUTH_ENDPOINT/signup")

        // then
        response.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "auth/signup",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("username").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("UUID"),
                    ),
                ),
            )
    }

    @Test
    fun `Login v1 200 OK`() {
        // given
        every { authService.loginUser(any()) } returns UserInfoDto(
            id = UUID.randomUUID(),
            username = "seongil-kim",
            email = "seongil.kim@gmail.com",
            role = Role.ROLE_USER,
            accessToken = "accessToken",
            refreshToken = "refreshToken",
        )
        val userLoginRequestDto = UserLoginRequestDto(
            email = "test@test.com",
            password = "Test123@!",
        )

        // when
        val response = mockMvc.body(userLoginRequestDto).request(Method.POST, "$AUTH_ENDPOINT/login")

        // then
        response.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "auth/login",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("회원의 UUID"),
                        fieldWithPath("data.username").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("data.role").type(JsonFieldType.STRING).description("권한"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                            .description("Access 토큰 (JWT)"),
                    ),
                    responseHeaders(
                        headerWithName(HttpHeaders.SET_COOKIE).description("Refresh 토큰 쿠키 세팅 ( JWT )"),
                    ),
                ),
            )
    }

    @Test
    fun `Refresh with not expired refresh token v1 200 OK`() {
        // given
        every { authService.refreshUserToken(any()) } returns TokenDto(
            accessToken = "accessToken",
            refreshToken = "refreshToken",
        )

        // when
        val response = mockMvc.request(Method.GET, "$AUTH_ENDPOINT/refresh")

        // then
        response.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "auth/refresh",
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("만료된 Access 토큰 ( JWT )"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                            .description("Access 토큰 (JWT)"),
                    ),
                    responseHeaders(
                        headerWithName(HttpHeaders.SET_COOKIE).description("새로운 Refresh 토큰 쿠키 세팅 ( JWT )"),
                    ),
                ),
            )
    }

    @Test
    fun `Get userInfo v1 200 OK`() {
        // when
        val result = mockMvc.request(Method.GET, "$AUTH_ENDPOINT/info")

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "auth/getUserInfo",
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("Access 토큰 ( JWT )"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("회원의 UUID"),
                        fieldWithPath("data.username").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("data.role").type(JsonFieldType.STRING).description("권한"),
                    ),
                ),
            )
    }

    @Test
    fun `Change password 200 OK`() {
        // given
        every { authService.changePassword(any(), any()) } returns true
        val passwordChangeRequestDto = PasswordChangeRequestDto("123456", "Test123@!")

        // when
        val result = mockMvc.body(passwordChangeRequestDto).request(Method.PUT, "$AUTH_ENDPOINT/password/change")

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "password/change",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data").type(JsonFieldType.STRING).description("비밀번호 변경 결과"),
                    ),
                ),
            )
    }

    @Test
    fun `Get password change code 200 OK`() {
        // given
        coJustRun { mailService.sendPasswordChangeMail(any()) }
        val passwordChangeRequestDto = PasswordChangeMailRequestDto("test@test.com")

        // when
        val result = mockMvc.body(passwordChangeRequestDto).request(Method.POST, "$AUTH_ENDPOINT/password/code")

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "password/code",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data").type(JsonFieldType.STRING).description("비밀번호 변경 요청 결과"),
                    ),
                ),
            )
    }
}
