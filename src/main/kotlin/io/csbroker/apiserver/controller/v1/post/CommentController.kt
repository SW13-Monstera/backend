package io.csbroker.apiserver.controller.v1.post

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.controller.v1.post.request.CommentCreateRequestDto
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.service.post.CommentService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentController(
    private val commentService: CommentService,
) {
    @DeleteMapping("/api/v1/comments/{id}")
    fun deleteCommentById(
        @LoginUser user: User,
        @PathVariable id: Long,
    ): ApiResponse<Unit> {
        commentService.deleteById(id, user)
        return ApiResponse.success()
    }

    @PostMapping("/api/v1/comments")
    fun createComment(
        @LoginUser loginUser: User,
        @RequestBody commentCreateRequestDto: CommentCreateRequestDto,
    ): ApiResponse<Long> {
        val postId = commentService.create(
            commentCreateRequestDto.postId,
            commentCreateRequestDto.content,
            loginUser,
        )
        return ApiResponse.success(postId)
    }
}
