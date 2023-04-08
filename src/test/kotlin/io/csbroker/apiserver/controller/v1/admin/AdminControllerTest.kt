package io.csbroker.apiserver.controller.v1.admin

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.notification.NotificationBulkInsertDto
import io.csbroker.apiserver.dto.notification.NotificationRequestDto
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
import org.springframework.restdocs.payload.PayloadDocumentation
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

    @Test
    fun `Create Notification 200`() {
        // given
        every { notificationService.createNotification(any()) } returns 1L

        // when
        val result = mockMvc.body(
            NotificationRequestDto(
                "알림이 왓어요",
                UUID.randomUUID(),
                "url",
            ),
        ).request(Method.POST, "/api/admin/notification")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/notifications/insert",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    PayloadDocumentation.requestFields(
                        fieldWithPath("content").type(JsonFieldType.STRING)
                            .description("알림 내용"),
                        fieldWithPath("userId").type(JsonFieldType.STRING)
                            .description("유저 아이디 ( UUID )"),
                        fieldWithPath("link").type(JsonFieldType.STRING)
                            .description("알림에 해당하는 링크"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("알림 ID"),
                    ),
                ),
            )
    }

    @Test
    fun `Create Multiple Notification 200`() {
        // given
        every { notificationService.createBulkNotification(any()) } returns 1

        // when
        val result = mockMvc.body(
            NotificationBulkInsertDto(
                listOf(
                    NotificationRequestDto(
                        "알림이 왔어요",
                        UUID.randomUUID(),
                        "url",
                    ),
                ),
            ),
        ).request(Method.POST, "/api/admin/notifications")

        // then
        result.then().statusCode(200)
            .apply(
                document(
                    "admin/notifications/bulkInsert",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    PayloadDocumentation.requestFields(
                        fieldWithPath("content").type(JsonFieldType.ARRAY)
                            .description("알림 데이터"),
                        fieldWithPath("content.[].content").type(JsonFieldType.STRING)
                            .description("알림 내용"),
                        fieldWithPath("content.[].userId").type(JsonFieldType.STRING)
                            .description("유저 아이디 ( UUID )"),
                        fieldWithPath("content.[].link").type(JsonFieldType.STRING)
                            .description("알림에 해당하는 링크"),
                    ),
                    responseFields(
                        fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.size")
                            .type(JsonFieldType.NUMBER).description("생성 된 알림 개수"),
                    ),
                ),
            )
    }
}
