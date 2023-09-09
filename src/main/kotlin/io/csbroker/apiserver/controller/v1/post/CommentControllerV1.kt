package io.csbroker.apiserver.controller.v1.post

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.service.post.CommentService
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentControllerV1(
    private val commentService: CommentService,
) {
    @DeleteMapping("/api/v1/comments/{id}")
    fun deleteCommentById(
        @LoginUser user: User,
        @PathVariable id: Long,
    ): ApiResponse<Unit> {
        commentService.deleteById(id, user.username)
        return ApiResponse.success()
    }
}
