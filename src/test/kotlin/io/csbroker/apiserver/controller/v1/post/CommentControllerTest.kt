package io.csbroker.apiserver.controller.v1.post

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.controller.v1.post.request.CommentCreateRequestDto
import io.csbroker.apiserver.service.post.CommentService
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.restassured.http.Method
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.request.RequestDocumentation

class CommentControllerTest : RestDocsTest() {
    private lateinit var commentService: CommentService
    private lateinit var mockMvc: MockMvcRequestSpecification

    @BeforeEach
    fun setUp() {
        commentService = mockk()
        mockMvc = mockMvc(
            CommentController(commentService),
        )
    }

    @Test
    fun `Delete Comment By Id 200`() {
        // given
        justRun { commentService.deleteById(any(), any()) }

        // when
        val result = mockMvc.request(Method.DELETE, "/api/v1/comments/{commentId}", 1L)

        // then
        result.then().statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "posts/comments/delete",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("commentId").description("댓글 id"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                    ),
                ),
            )
    }

    @Test
    fun `Create Comment 200`() {
        // given
        every { commentService.create(any(), any(), any()) } returns 1L

        // when
        val result = mockMvc.body(
            CommentCreateRequestDto(
                1L,
                "content",
            ),
        ).request(Method.POST, "/api/v1/comments")

        // then
        result.then().statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "posts/comments/create",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("postId")
                            .type(JsonFieldType.NUMBER).description("글 id"),
                        PayloadDocumentation.fieldWithPath("content").type(JsonFieldType.STRING)
                            .description("댓글 내용"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data")
                            .type(JsonFieldType.NUMBER).description("댓글 ID"),
                    ),
                ),
            )
    }

    @Test
    fun `댓글 좋아요`() {
        // given
        justRun { commentService.like(any(), any()) }

        // when
        val result = mockMvc.request(Method.POST, "/api/v1/comments/{commentId}/like", 1L)

        // then
        result.then().statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "comments/like",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("commentId").description("댓글 id"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                    ),
                ),
            )
    }
}
