package io.csbroker.apiserver.controller.v1.admin

import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.common.UpsertSuccessResponseDto
import io.csbroker.apiserver.dto.notification.NotificationBulkInsertDto
import io.csbroker.apiserver.dto.notification.NotificationRequestDto
import io.csbroker.apiserver.dto.user.AdminUserInfoResponseDto
import io.csbroker.apiserver.service.common.NotificationService
import io.csbroker.apiserver.service.user.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val userService: UserService,
    private val notificationService: NotificationService,
) {
    @GetMapping("/users/admin")
    fun findAdminUsers(): ApiResponse<List<AdminUserInfoResponseDto>> {
        val adminUserInfoResponseDtoList = userService.findAdminUsers().map {
            AdminUserInfoResponseDto(
                it.id!!,
                it.username,
            )
        }

        return ApiResponse.success(adminUserInfoResponseDtoList)
    }

    @PostMapping("/notification")
    fun createNotification(
        @RequestBody createNotificationDto: NotificationRequestDto,
    ): ApiResponse<UpsertSuccessResponseDto> {
        return ApiResponse.success(
            UpsertSuccessResponseDto(
                id = notificationService.createNotification(createNotificationDto),
            ),
        )
    }

    @PostMapping("/notifications")
    fun createBulkNotifications(
        @RequestBody notificationBulkInsertDto: NotificationBulkInsertDto,
    ): ApiResponse<UpsertSuccessResponseDto> {
        return ApiResponse.success(
            UpsertSuccessResponseDto(
                size = notificationService.createBulkNotification(notificationBulkInsertDto.content),
            ),
        )
    }
}
