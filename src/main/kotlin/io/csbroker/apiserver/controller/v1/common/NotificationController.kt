package io.csbroker.apiserver.controller.v1.common

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.notification.NotificationBulkDeleteDto
import io.csbroker.apiserver.dto.notification.NotificationBulkReadDto
import io.csbroker.apiserver.dto.notification.NotificationPageResponseDto
import io.csbroker.apiserver.dto.notification.NotificationReadResponseDto
import io.csbroker.apiserver.dto.notification.UnReadNotificationCountDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.service.common.NotificationService
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService,
) {
    @GetMapping
    fun getNotifications(
        @LoginUser loginUser: User,
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC,
        ) pageable: Pageable,
    ): ApiResponse<NotificationPageResponseDto> {
        val notifications = notificationService.getNotification(loginUser.id!!, pageable)
        return ApiResponse.success(NotificationPageResponseDto(notifications))
    }

    @GetMapping("/count")
    fun getUnreadNotificationCount(@LoginUser loginUser: User): ApiResponse<UnReadNotificationCountDto> {
        val count = notificationService.getUnreadNotificationCount(loginUser.id!!)
        return ApiResponse.success(UnReadNotificationCountDto(count))
    }

    @PutMapping("/read")
    fun readNotifications(
        @LoginUser loginUser: User,
        @RequestBody notificationBulkReadDto: NotificationBulkReadDto,
    ): ApiResponse<NotificationReadResponseDto> {
        notificationService.readNotifications(loginUser.id!!, notificationBulkReadDto.ids)
        return ApiResponse.success(NotificationReadResponseDto())
    }

    @PutMapping("/read/{id}")
    fun readNotificationById(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
    ): ApiResponse<NotificationReadResponseDto> {
        notificationService.readNotificationById(loginUser.id!!, id)
        return ApiResponse.success(NotificationReadResponseDto())
    }

    @DeleteMapping
    fun deleteNotificationByIds(
        @LoginUser loginUser: User,
        @RequestBody notificationBulkDeleteDto: NotificationBulkDeleteDto,
    ): ApiResponse<Boolean> {
        notificationService.deleteNotifications(loginUser, notificationBulkDeleteDto.ids)
        return ApiResponse.success(true)
    }
}
