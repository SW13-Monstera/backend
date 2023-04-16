package io.csbroker.apiserver.controller.v1.common

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.notification.NotificationBulkDeleteDto
import io.csbroker.apiserver.dto.notification.NotificationBulkReadDto
import io.csbroker.apiserver.model.Notification
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.service.common.NotificationService
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.restassured.http.Method
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.headers.HeaderDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.request.RequestDocumentation
import java.time.LocalDateTime
import java.util.UUID

class NotificationControllerTest : RestDocsTest() {
    private val NOTIFICATION_ENDPOINT = "/api/v1/notifications"
    private lateinit var mockMvc: MockMvcRequestSpecification
    private lateinit var notificationService: NotificationService

    @BeforeEach
    fun setup() {
        notificationService = mockk()
        mockMvc = mockMvc(
            NotificationController(
                notificationService,
            ),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `Get Notifications 200`() {
        // given
        val page = 0
        val size = 10
        every { notificationService.getNotification(any(), any()) } returns PageImpl(
            listOf(
                Notification(
                    id = 1,
                    content = "content",
                    isRead = false,
                    link = "link",
                    user = User(
                        id = UUID.randomUUID(),
                        email = "email",
                        username = "username",
                        providerType = ProviderType.LOCAL,
                    ),
                ).also { it.createdAt = LocalDateTime.now() },
            ),
            PageRequest.of(0, 10),
            1L,
        )

        // when
        val result = mockMvc.request(Method.GET, "$NOTIFICATION_ENDPOINT?page=$page&size=$size")

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "notifications/getAll",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    HeaderDocumentation.requestHeaders(
                        HeaderDocumentation.headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰")
                            .optional(),
                    ),
                    RequestDocumentation.requestParameters(
                        RequestDocumentation.parameterWithName("page").description("페이지"),
                        RequestDocumentation.parameterWithName("size").description("가져올 문제의 개수"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.contents")
                            .type(JsonFieldType.ARRAY).description("알림 내용 리스트"),
                        PayloadDocumentation.fieldWithPath("data.contents.[].id")
                            .type(JsonFieldType.NUMBER).description("알림 ID"),
                        PayloadDocumentation.fieldWithPath("data.contents.[].content")
                            .type(JsonFieldType.STRING).description("알림 내용"),
                        PayloadDocumentation.fieldWithPath("data.contents.[].link")
                            .type(JsonFieldType.STRING).description("알림 링크"),
                        PayloadDocumentation.fieldWithPath("data.contents.[].isRead")
                            .type(JsonFieldType.BOOLEAN).description("알림 읽음 여부"),
                        PayloadDocumentation.fieldWithPath("data.contents.[].createdAt")
                            .type(JsonFieldType.STRING).description("알림 생성 시간"),
                        PayloadDocumentation.fieldWithPath("data.currentPage")
                            .type(JsonFieldType.NUMBER).description("요청한 현재 페이지"),
                        PayloadDocumentation.fieldWithPath("data.totalPages")
                            .type(JsonFieldType.NUMBER).description("총 페이지"),
                        PayloadDocumentation.fieldWithPath("data.totalElements")
                            .type(JsonFieldType.NUMBER).description("총 알림 개수"),
                        PayloadDocumentation.fieldWithPath("data.numberOfElements")
                            .type(JsonFieldType.NUMBER).description("현재 페이지의 알림 개수"),
                        PayloadDocumentation.fieldWithPath("data.size")
                            .type(JsonFieldType.NUMBER).description("한 페이지에 보여줄 알림 개수"),
                    ),
                ),
            )
    }

    @Test
    fun `Read One Notification 200`() {
        // given
        justRun { notificationService.readNotificationById(any(), any()) }

        // when
        val result = mockMvc.request(Method.PUT, "$NOTIFICATION_ENDPOINT/read/{notification_id}", 1L)

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "notifications/readOne",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),

                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.success")
                            .type(JsonFieldType.BOOLEAN).description("읽음 처리 성공 여부"),
                    ),
                ),
            )
    }

    @Test
    fun `Read Notifications 200`() {
        // given
        justRun { notificationService.readNotifications(any(), any()) }

        // when
        val result = mockMvc.body(NotificationBulkReadDto(ids = listOf(1L)))
            .request(Method.PUT, "$NOTIFICATION_ENDPOINT/read")

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "notifications/readBulk",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("ids")
                            .type(JsonFieldType.ARRAY).description("읽음 처리할 알림 ID 리스트"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.success")
                            .type(JsonFieldType.BOOLEAN).description("읽음 처리 성공 여부"),
                    ),
                ),
            )
    }

    @Test
    fun `Get Un Read Notifications 200`() {
        // given
        every { notificationService.getUnreadNotificationCount(any()) } returns 10

        // when
        val result = mockMvc.request(Method.GET, "$NOTIFICATION_ENDPOINT/count")

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "notifications/count",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    HeaderDocumentation.requestHeaders(
                        HeaderDocumentation.headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰")
                            .optional(),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.count")
                            .type(JsonFieldType.NUMBER).description("읽지 않은 알림 개수"),
                    ),
                ),
            )
    }

    @Test
    fun `Delete notifications 200`() {
        // given
        justRun { notificationService.deleteNotifications(any(), any()) }

        // when
        val result = mockMvc.body(NotificationBulkDeleteDto(ids = listOf(1L)))
            .request(Method.DELETE, NOTIFICATION_ENDPOINT)

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "notifications/delete",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("ids")
                            .type(JsonFieldType.ARRAY).description("삭제할 알림 ID 리스트"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data")
                            .type(JsonFieldType.BOOLEAN).description("읽음 처리 성공 여부"),
                    ),
                ),
            )
    }
}
