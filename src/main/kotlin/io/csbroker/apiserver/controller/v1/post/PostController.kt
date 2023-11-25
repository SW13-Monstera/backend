package io.csbroker.apiserver.controller.v1.post

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.common.util.getEmailFromSecurityContextHolder
import io.csbroker.apiserver.controller.v1.post.request.PostCreateRequestDto
import io.csbroker.apiserver.controller.v1.post.response.PostResponseDto
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.service.post.PostService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PostController(
    private val postService: PostService,
) {
    @PostMapping("/api/v1/posts")
    fun createPost(
        @LoginUser loginUser: User,
        @RequestBody postCreateRequestDto: PostCreateRequestDto,
    ): ApiResponse<Long> {
        val postId = postService.create(
            postCreateRequestDto.problemId,
            postCreateRequestDto.content,
            loginUser,
        )
        return ApiResponse.success(postId)
    }

    @DeleteMapping("/api/v1/posts/{id}")
    fun deletePost(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
    ): ApiResponse<Unit> {
        postService.deleteById(id, loginUser)
        return ApiResponse.success()
    }

    @GetMapping("/api/v1/problems/{problemId}/posts")
    fun findAllByProblemId(
        @PathVariable("problemId") id: Long,
    ): ApiResponse<List<PostResponseDto>> {
        val nullableEmail = getEmailFromSecurityContextHolder()
        return ApiResponse.success(postService.findByProblemId(id, nullableEmail))
    }

    @PostMapping("/api/v1/posts/{id}/like")
    fun likePost(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
    ): ApiResponse<Unit> {
        postService.like(id, loginUser)
        return ApiResponse.success()
    }
}
