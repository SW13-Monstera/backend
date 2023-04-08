package io.csbroker.apiserver.e2e.admin

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.controller.v1.admin.AdminController
import io.csbroker.apiserver.e2e.RestDocsTest
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.service.common.NotificationService
import io.csbroker.apiserver.service.user.UserService
import io.mockk.every
import io.mockk.mockk
import io.restassured.http.Method
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import java.util.UUID

class AdminControllerTest : RestDocsTest() {
    private lateinit var userService: UserService
    private lateinit var notificationService: NotificationService
    private lateinit var mockMvc: MockMvcRequestSpecification

    @BeforeEach
    fun setUp() {
        userService = mockk()
        notificationService = mockk()
        mockMvc = mockMvc(
            AdminController(userService, notificationService),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `Search Admin Users 200`() {
        // given
        every { userService.findAdminUsers() } returns listOf(
            User(
                UUID.randomUUID(),
                "nickname",
                "email",
                "password",
                Role.ROLE_ADMIN,
                ProviderType.LOCAL,
                "blogUrl",
                "description",
            ),
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/admin/users/admin")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/user/admin/search",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING)
                            .description("결과 상태"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY)
                            .description("ADMIN 유저 정보"),
                        fieldWithPath("data.[].id").type(JsonFieldType.STRING)
                            .description("유저 ID"),
                        fieldWithPath("data.[].username")
                            .type(JsonFieldType.STRING)
                            .description("유저 닉네임"),
                    ),
                ),
            )
    }
}
