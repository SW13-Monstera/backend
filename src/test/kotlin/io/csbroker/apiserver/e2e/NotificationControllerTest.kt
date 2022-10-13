package io.csbroker.apiserver.e2e

import com.fasterxml.jackson.databind.ObjectMapper
import io.csbroker.apiserver.auth.AuthTokenProvider
import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.dto.notification.NotificationBulkInsertDto
import io.csbroker.apiserver.dto.notification.NotificationBulkReadDto
import io.csbroker.apiserver.dto.notification.NotificationRequestDto
import io.csbroker.apiserver.model.Notification
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.NotificationRepository
import io.csbroker.apiserver.repository.UserRepository
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.Date

@SpringBootTest
@AutoConfigureRestDocs
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    @Autowired
    private lateinit var tokenProvider: AuthTokenProvider

    private lateinit var user: User

    private lateinit var token: String

    private val ADMIN_ENDPOINT = "/api/admin"

    private val NOTIFICATION_ENDPOINT = "/api/v1/notifications"

    @BeforeAll
    fun setup() {
        val user = User(
            email = "test-admin3@test.com",
            username = "test-admin3",
            providerType = ProviderType.LOCAL,
            role = Role.ROLE_ADMIN
        )

        userRepository.save(user)

        this.user = user

        val now = Date()

        val accessToken = tokenProvider.createAuthToken(
            "test-admin3@test.com",
            expiry = Date(now.time + 6000000),
            role = Role.ROLE_ADMIN.code
        )

        this.token = accessToken.token
    }

    @Test
    @Order(1)
    fun `Create Notification 200`() {
        // given
        val notificationRequestDto = NotificationRequestDto(
            content = "test content",
            userId = user.id!!,
            link = "https://dev.csbroker.io"
        )

        val notificationRequestDtoString = objectMapper.writeValueAsString(notificationRequestDto)

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("$ADMIN_ENDPOINT/notification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notificationRequestDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/notifications/insert",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("content").type(JsonFieldType.STRING)
                            .description("알림 내용"),
                        PayloadDocumentation.fieldWithPath("userId").type(JsonFieldType.STRING)
                            .description("유저 아이디 ( UUID )"),
                        PayloadDocumentation.fieldWithPath("link").type(JsonFieldType.STRING)
                            .description("알림에 해당하는 링크")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("알림 ID")
                    )
                )
            )
    }

    @Test
    @Order(2)
    fun `Create Multiple Notification 200`() {
        // given
        val content = mutableListOf<NotificationRequestDto>()

        for (i in 1..10) {
            content.add(
                NotificationRequestDto(
                    content = "test content $i",
                    userId = user.id!!,
                    link = "https://dev.csbroker.io"
                )
            )
        }

        val bulkInsertDto = NotificationBulkInsertDto(content)

        val bulkInsertDtoString = objectMapper.writeValueAsString(bulkInsertDto)

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("$ADMIN_ENDPOINT/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bulkInsertDtoString)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "admin/notifications/bulkInsert",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("content").type(JsonFieldType.ARRAY)
                            .description("알림 데이터"),
                        PayloadDocumentation.fieldWithPath("content.[].content").type(JsonFieldType.STRING)
                            .description("알림 내용"),
                        PayloadDocumentation.fieldWithPath("content.[].userId").type(JsonFieldType.STRING)
                            .description("유저 아이디 ( UUID )"),
                        PayloadDocumentation.fieldWithPath("content.[].link").type(JsonFieldType.STRING)
                            .description("알림에 해당하는 링크")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.size")
                            .type(JsonFieldType.NUMBER).description("생성 된 알림 개수")
                    )
                )
            )
    }

    @Test
    @Order(3)
    fun `Get Notifications 200`() {
        // given
        val page = 0
        val size = 10

        notificationRepository.saveAll(
            (1..10).map {
                Notification(
                    content = "test content $it",
                    user = this.user,
                    link = "https://dev.csbroker.io"
                )
            }.toList()
        )

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("$NOTIFICATION_ENDPOINT?page=$page&size=$size")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "notifications/getAll",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    HeaderDocumentation.requestHeaders(
                        HeaderDocumentation.headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰")
                            .optional()
                    ),
                    RequestDocumentation.requestParameters(
                        RequestDocumentation.parameterWithName("page").description("페이지"),
                        RequestDocumentation.parameterWithName("size").description("가져올 문제의 개수")
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
                        PayloadDocumentation.fieldWithPath("data.currentPage")
                            .type(JsonFieldType.NUMBER).description("요청한 현재 페이지"),
                        PayloadDocumentation.fieldWithPath("data.totalPages")
                            .type(JsonFieldType.NUMBER).description("총 페이지"),
                        PayloadDocumentation.fieldWithPath("data.totalElements")
                            .type(JsonFieldType.NUMBER).description("총 알림 개수"),
                        PayloadDocumentation.fieldWithPath("data.numberOfElements")
                            .type(JsonFieldType.NUMBER).description("현재 페이지의 알림 개수"),
                        PayloadDocumentation.fieldWithPath("data.size")
                            .type(JsonFieldType.NUMBER).description("한 페이지에 보여줄 알림 개수")
                    )
                )
            )
    }

    @Test
    @Order(4)
    fun `Read One Notification 200`() {
        // given
        val id = notificationRepository.save(
            Notification(
                content = "test content",
                user = this.user,
                link = "https://dev.csbroker.io"
            )
        ).id!!

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.put("$NOTIFICATION_ENDPOINT/read/{notification_id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "notifications/readOne",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.id")
                            .type(JsonFieldType.NUMBER).description("알림 ID")
                    )
                )
            )
    }

    @Test
    @Order(5)
    fun `Read Notifications 200`() {
        // given
        val ids = notificationRepository.saveAll(
            (1..10).map {
                Notification(
                    content = "test content $it",
                    user = this.user,
                    link = "https://dev.csbroker.io"
                )
            }.toList()
        ).map {
            it.id!!
        }.toList()

        val notificationBulkReadDto = NotificationBulkReadDto(
            ids = ids
        )

        val notificationBulkReadDtoString = objectMapper.writeValueAsString(notificationBulkReadDto)

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.put("$NOTIFICATION_ENDPOINT/read")
                .content(notificationBulkReadDtoString)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
                MockMvcRestDocumentation.document(
                    "notifications/readBulk",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("ids")
                            .type(JsonFieldType.ARRAY).description("읽음 처리할 알림 ID 리스트")
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.size")
                            .type(JsonFieldType.NUMBER).description("읽음 처리된 알림 개수")
                    )
                )
            )
    }
}
