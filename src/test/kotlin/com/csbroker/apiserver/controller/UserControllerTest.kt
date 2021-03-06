package com.csbroker.apiserver.controller

import com.csbroker.apiserver.common.auth.AuthTokenProvider
import com.csbroker.apiserver.common.auth.ProviderType
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.dto.UserUpdateRequestDto
import com.csbroker.apiserver.model.User
import com.csbroker.apiserver.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Date
import java.util.UUID

@SpringBootTest
@AutoConfigureRestDocs
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var tokenProvider: AuthTokenProvider

    @Autowired
    private lateinit var userRepository: UserRepository

    private val USER_ENDPOINT = "/api/users"

    private lateinit var adminId: UUID

    @BeforeAll
    fun setUpData() {
        for (i in 1..10) {
            val user = User(
                email = "test-user$i@test.com",
                username = "test-user$i",
                providerType = ProviderType.LOCAL
            )
            userRepository.save(user)
        }

        val admin = User(
            email = "test-admin@test.com",
            username = "test-admin",
            role = Role.ROLE_ADMIN,
            providerType = ProviderType.LOCAL
        )

        userRepository.save(admin)

        adminId = admin.id!!
    }

    @Test
    fun `GetUser v1 200 OK`() {
        // given
        val now = Date()
        val urlTemplate = "$USER_ENDPOINT/{user_id}"

        val accessToken = tokenProvider.createAuthToken(
            "test-admin@test.com",
            expiry = Date(now.time + 6000000),
            role = Role.ROLE_ADMIN.code
        )

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(urlTemplate, "$adminId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(content().string(containsString("success")))
            .andDo(
                document(
                    "users/findOne",
                    preprocessResponse(Preprocessors.prettyPrint()),
                    pathParameters(
                        parameterWithName("user_id").description("?????? UUID")
                    ),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Access ?????? ( JWT )")
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
    fun `GetUsers v1 200 OK`() {
        // given
        val now = Date()
        val accessToken = tokenProvider.createAuthToken(
            "test-admin@test.com",
            expiry = Date(now.time + 6000000),
            role = Role.ROLE_ADMIN.code
        )

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get(USER_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(content().string(containsString("success")))
            .andDo(
                document(
                    "users/findAll",
                    preprocessResponse(Preprocessors.prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Admin ????????? Access ?????? ( JWT )")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("?????? ??????"),
                        fieldWithPath("data.[].id").type(JsonFieldType.STRING).description("UUID"),
                        fieldWithPath("data.[].email").type(JsonFieldType.STRING).description("?????????"),
                        fieldWithPath("data.[].username").type(JsonFieldType.STRING).description("?????????"),
                        fieldWithPath("data.[].role").type(JsonFieldType.STRING).description("??????")
                    )
                )
            )
    }

    @Test
    fun `UpdateUser v1 200 OK`() {
        // given
        val now = Date()
        val urlTemplate = "$USER_ENDPOINT/$adminId"
        val accessToken = tokenProvider.createAuthToken(
            "test-admin@test.com",
            expiry = Date(now.time + 6000000),
            role = Role.ROLE_ADMIN.code
        )
        val userUpdateRequestDto = UserUpdateRequestDto(
            username = "test-admin-update",
            profileImageUrl = "https://test.com/test.png",
            password = "changePassword123!"
        )

        val userUpdateRequestDtoString = objectMapper.writeValueAsString(userUpdateRequestDto)

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.patch(urlTemplate)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userUpdateRequestDtoString)
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(content().string(containsString("success")))
            .andDo(
                document(
                    "users/update",
                    preprocessRequest(Preprocessors.prettyPrint()),
                    preprocessResponse(Preprocessors.prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Access ?????? ( JWT )")
                    ),
                    PayloadDocumentation.requestFields(
                        fieldWithPath("username").type(JsonFieldType.STRING)
                            .description("????????? ????????? ( ?????? X )").optional(),
                        fieldWithPath("profileImageUrl").type(JsonFieldType.STRING)
                            .description("????????? ????????? ????????? url ( ?????? X )").optional(),
                        fieldWithPath("password").type(JsonFieldType.STRING)
                            .description("????????? ???????????? ( ?????? X )").optional()
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
}
