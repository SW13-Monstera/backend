package com.csbroker.apiserver.controller

import com.csbroker.apiserver.dto.UserLoginRequestDto
import com.csbroker.apiserver.dto.UserSignUpDto
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureRestDocs
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("username").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("UUID"),
                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("data.username").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("data.role").type(JsonFieldType.STRING).description("권한")
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
                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                            .description("Access 토큰 (JWT)")
                    ),
                    responseHeaders(
                        headerWithName(HttpHeaders.SET_COOKIE).description("Refresh 토큰 쿠키 세팅 ( JWT )")
                    )
                )
            )
    }
}
