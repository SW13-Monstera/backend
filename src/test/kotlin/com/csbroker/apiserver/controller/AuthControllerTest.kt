package com.csbroker.apiserver.controller

import com.csbroker.apiserver.common.auth.AuthTokenProvider
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.dto.UserLoginRequestDto
import com.csbroker.apiserver.dto.UserSignUpDto
import com.csbroker.apiserver.repository.REFRESH_TOKEN
import com.csbroker.apiserver.repository.RedisRepository
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Date
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

    private val AUTH_ENDPOINT = "/api/auth"

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
            .andExpect(MockMvcResultMatchers.content().string(containsString("test")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "auth/signup",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("email").type(JsonFieldType.STRING).description("?????????"),
                        fieldWithPath("username").type(JsonFieldType.STRING).description("?????????"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("????????????")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("?????? ??????"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("UUID"),
                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("?????????"),
                        fieldWithPath("data.username").type(JsonFieldType.STRING).description("?????????"),
                        fieldWithPath("data.role").type(JsonFieldType.STRING).description("??????")
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
                        fieldWithPath("email").type(JsonFieldType.STRING).description("?????????"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("????????????")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("?????? ??????"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("????????? UUID"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                            .description("Access ?????? (JWT)")
                    ),
                    responseHeaders(
                        headerWithName(HttpHeaders.SET_COOKIE).description("Refresh ?????? ?????? ?????? ( JWT )")
                    )
                )
            )
    }

    @Test
    @Order(3)
    fun `Refresh with not expired refresh token v1 200 OK`() {
        // given
        // ????????? ??????
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
                        headerWithName(HttpHeaders.AUTHORIZATION).description("????????? Access ?????? ( JWT )")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("?????? ??????"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                            .description("Access ?????? (JWT)")
                    ),
                    responseHeaders(
                        headerWithName(HttpHeaders.SET_COOKIE).description("????????? Refresh ?????? ?????? ?????? ( JWT )")
                    )
                )
            )
    }
}
