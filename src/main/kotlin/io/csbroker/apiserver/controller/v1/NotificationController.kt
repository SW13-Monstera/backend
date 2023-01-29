package io.csbroker.apiserver.controller.v1

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.notification.NotificationBulkReadDto
import io.csbroker.apiserver.dto.notification.NotificationPageResponseDto
import io.csbroker.apiserver.dto.notification.NotificationReadResponseDto
import io.csbroker.apiserver.dto.notification.UnReadNotificationCountDto
import io.csbroker.apiserver.service.NotificationService
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping
    fun getNotifications(
        @LoginUser loginUser: User,
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ApiResponse<NotificationPageResponseDto> {
        val notifications = notificationService.getNotification(loginUser.username, pageable)
        return ApiResponse.success(NotificationPageResponseDto(notifications))
    }

    @GetMapping("/count")
    fun getUnreadNotificationCount(@LoginUser loginUser: User): ApiResponse<UnReadNotificationCountDto> {
        val count = notificationService.getUnreadNotificationCount(loginUser.username)
        return ApiResponse.success(UnReadNotificationCountDto(count))
    }

    @PutMapping("/read")
    fun readNotifications(
        @LoginUser loginUser: User,
        @RequestBody notificationBulkReadDto: NotificationBulkReadDto
    ): ApiResponse<NotificationReadResponseDto> {
        notificationService.readNotifications(loginUser.username, notificationBulkReadDto.ids)
        return ApiResponse.success(NotificationReadResponseDto())
    }

    @PutMapping("/read/{id}")
    fun readNotificationById(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long
    ): ApiResponse<NotificationReadResponseDto> {
        notificationService.readNotificationById(loginUser.username, id)
        return ApiResponse.success(NotificationReadResponseDto())
    }
}