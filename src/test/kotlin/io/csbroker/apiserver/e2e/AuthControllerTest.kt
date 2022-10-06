package io.csbroker.apiserver.e2e

import io.csbroker.apiserver.auth.AuthTokenProvider
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.dto.auth.PasswordChangeMailRequestDto
import io.csbroker.apiserver.dto.auth.PasswordChangeRequestDto
import io.csbroker.apiserver.dto.user.UserLoginRequestDto
import io.csbroker.apiserver.dto.user.UserSignUpDto
import io.csbroker.apiserver.repository.common.REFRESH_TOKEN
import io.csbroker.apiserver.repository.common.RedisRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Date
import java.util.UUID
import javax.servlet.http.Cookie

@SpringBootTest
@AutoConfigureRestDocs
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var tokenProvider: AuthTokenProvider

    @Autowired
    private lateinit var redisRepository: RedisRepository

    private val AUTH_ENDPOINT = "/api/v1/auth"

    @Test
    @Order(1)
    fun `Signup v1 200 OK`() {
        // given
        val userSignUpDto = UserSignUpDto(
            username = "test",
            email = "test@test.com",
            password = "Test123@!"
        )

        val signUpDtoString = objectMapper.writeValueAsString(userSignUpDto)

        // when
        val result = mockMvc.perform(
            post("$AUTH_ENDPOINT/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signUpDtoString)
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "auth/signup",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("username").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("UUID")
                    )
                )
            )
    }

    @Test
    @Order(2)
    fun `Login v1 200 OK`() {
        // given
        val userLoginRequestDto = UserLoginRequestDto(
            email = "test@test.com",
            password = "Test123@!"
        )

        val loginDtoString = objectMapper.writeValueAsString(userLoginRequestDto)

        // when
        val result = mockMvc.perform(
            post("$AUTH_ENDPOINT/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginDtoString)
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(containsString("accessToken")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "auth/login",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("회원의 UUID"),
                        fieldWithPath("data.username").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("data.role").type(JsonFieldType.STRING).description("권한"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                            .description("Access 토큰 (JWT)")
                    ),
                    responseHeaders(
                        headerWithName(HttpHeaders.SET_COOKIE).description("Refresh 토큰 쿠키 세팅 ( JWT )")
                    )
                )
            )
    }

    @Test
    @Order(3)
    fun `Refresh with not expired refresh token v1 200 OK`() {
        // given
        // 로그인 가정
        val now = Date()
        val email = "test@test.com"

        val expiredAccessToken = tokenProvider.createAuthToken(
            email = email,
            expiry = now,
            role = Role.ROLE_USER.code
        )

        val refreshToken = tokenProvider.createAuthToken(
            email = email,
            expiry = Date(now.time + 259200000)
        )

        val refreshTokenCookie = Cookie(REFRESH_TOKEN, refreshToken.token)

        redisRepository.setRefreshTokenByEmail(email, refreshToken.token)

        // when
        val result = mockMvc.perform(
            get("$AUTH_ENDPOINT/refresh")
                .cookie(refreshTokenCookie)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${expiredAccessToken.token}")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(containsString("accessToken")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "auth/refresh",
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("만료된 Access 토큰 ( JWT )")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                            .description("Access 토큰 (JWT)")
                    ),
                    responseHeaders(
                        headerWithName(HttpHeaders.SET_COOKIE).description("새로운 Refresh 토큰 쿠키 세팅 ( JWT )")
                    )
                )
            )
    }

    @Test
    @Order(4)
    fun `Get userInfo v1 200 OK`() {
        // given
        // 로그인 가정
        val now = Date()
        val email = "test@test.com"

        val accessToken = tokenProvider.createAuthToken(
            email = email,
            expiry = Date(now.time + 259200000),
            role = Role.ROLE_USER.code
        )

        // when
        val result = mockMvc.perform(
            get("$AUTH_ENDPOINT/info")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(containsString("id")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "auth/getUserInfo",
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("Access 토큰 ( JWT )")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("회원의 UUID"),
                        fieldWithPath("data.username").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("data.role").type(JsonFieldType.STRING).description("권한")
                    )
                )
            )
    }

    @Test
    @Order(5)
    fun `Send password mail 200 OK`() {
        // given
        val email = "test@test.com"
        val passwordChangeMailRequestDto = PasswordChangeMailRequestDto(email)
        val passwordChangeMailRequestDtoString = objectMapper.writeValueAsString(passwordChangeMailRequestDto)

        // when
        val result = mockMvc.perform(
            post("$AUTH_ENDPOINT/password/code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordChangeMailRequestDtoString)
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "password/code",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data").type(JsonFieldType.STRING).description("메일 전송 결과")
                    )
                )
            )
    }

    @Test
    @Order(6)
    fun `Change password 200 OK`() {
        // given
        val email = "test@test.com"
        val code = UUID.randomUUID().toString()
        redisRepository.setPasswordVerification(code, email)
        val passwordChangeRequestDto = PasswordChangeRequestDto(code, "Test123@!")
        val passwordChangeRequestDtoString = objectMapper.writeValueAsString(passwordChangeRequestDto)

        // when
        val result = mockMvc.perform(
            put("$AUTH_ENDPOINT/password/change")
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordChangeRequestDtoString)
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "password/change",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data").type(JsonFieldType.STRING).description("비밀번호 변경 결과")
                    )
                )
            )
    }
}
