package io.csbroker.apiserver.controller.v1.admin

import com.jayway.jsonpath.JsonPath
import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.controller.IntegrationTest
import io.csbroker.apiserver.dto.notification.NotificationBulkInsertDto
import io.csbroker.apiserver.dto.notification.NotificationRequestDto
import io.csbroker.apiserver.model.Notification
import io.csbroker.apiserver.model.User
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class AdminControllerIntegrationTest : IntegrationTest() {
    @Test
    fun `어드민 유저 목록 조회`() {
        // given + when
        val response = request(
            method = HttpMethod.GET,
            url = "/api/admin/users/admin",
            isAdmin = true,
        )

        // then
        response.andExpect(status().isOk)
            .andExpect {
                val id = JsonPath.read(it.response.contentAsString, "$.data[0].id") as String
                val user = findOne<User>("SELECT u From User u where u.id = :id", mapOf("id" to UUID.fromString(id)))
                user.role shouldBe Role.ROLE_ADMIN
            }
    }

    @Test
    fun `단일 알림 생성`() {
        // given
        val user = save(
            User(
                email = "test-noti@test.com",
                username = "test-noti",
                password = "test-noti",
                providerType = ProviderType.LOCAL,
            ),
        )

        // when
        val response = request(
            method = HttpMethod.POST,
            url = "/api/admin/notification",
            body = NotificationRequestDto(
                content = "test",
                userId = user.id!!,
                link = "https://test.com",
            ),
            isAdmin = true,
        )

        // then
        response.andExpect(status().isOk)
            .andExpect {
                val id = JsonPath.read(it.response.contentAsString, "$.data.id") as Int
                val notification = findOne<Notification>(
                    "SELECT n FROM Notification n WHERE n.id = :id",
                    mapOf("id" to id.toLong()),
                )
                notification.content shouldBe "test"
                notification.link shouldBe "https://test.com"
                notification.user.id shouldBe user.id
            }
    }

    @Test
    fun `알림 여러개 생성`() {
        // given
        val user = save(
            User(
                email = "test-noti2@test.com",
                username = "test-noti2",
                password = "test-noti2",
                providerType = ProviderType.LOCAL,
            ),
        )

        // when
        val response = request(
            method = HttpMethod.POST,
            url = "/api/admin/notifications",
            body = NotificationBulkInsertDto(
                content = listOf(
                    NotificationRequestDto(
                        content = "test",
                        userId = user.id!!,
                        link = "https://test.com",
                    ),
                ),
            ),
            isAdmin = true,
        )

        // then
        response.andExpect(status().isOk)
            .andExpect {
                val size = JsonPath.read(it.response.contentAsString, "$.data.size") as Int
                size shouldBe 1
            }
    }
}
