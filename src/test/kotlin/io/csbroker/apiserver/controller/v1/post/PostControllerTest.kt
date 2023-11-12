package io.csbroker.apiserver.controller.v1.post

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.controller.v1.post.request.PostCreateRequestDto
import io.csbroker.apiserver.controller.v1.post.response.CommentResponseDto
import io.csbroker.apiserver.controller.v1.post.response.PostResponseDto
import io.csbroker.apiserver.service.post.PostService
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
import java.time.LocalDateTime

class PostControllerTest : RestDocsTest() {
    private lateinit var postService: PostService
    private lateinit var mockMvc: MockMvcRequestSpecification

    @BeforeEach
    fun setUp() {
        postService = mockk()
        mockMvc = mockMvc(
            PostController(postService),
        )
    }

    @Test
    fun `Create Post 200`() {
        // given
        every { postService.create(any(), any(), any()) } returns 1L

        // when
        val result = mockMvc.body(
            PostCreateRequestDto(
                1L,
                "content",
            ),
        ).request(Method.POST, "/api/v1/posts")

        // then
        result.then().statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "posts/create",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("problemId").type(JsonFieldType.NUMBER)
                            .description("문제 ID"),
                        PayloadDocumentation.fieldWithPath("content").type(JsonFieldType.STRING)
                            .description("글 내용"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data")
                            .type(JsonFieldType.NUMBER).description("글 ID"),
                    ),
                ),
            )
    }

    @Test
    fun `Delete Post 200`() {
        // given
        justRun { postService.deleteById(any(), any()) }

        // when
        val result = mockMvc.request(Method.DELETE, "/api/v1/posts/{postId}", "1")

        // then
        result.then().statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "posts/delete",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("postId").description("글 id"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                    ),
                ),
            )
    }

    @Test
    fun `Get Posts 200`() {
        // given
        every { postService.findByProblemId(any(), any()) } returns listOf(
            PostResponseDto(
                1L,
                "CONTENT",
                "USER",
                1L,
                true,
                listOf(
                    CommentResponseDto(
                        1L,
                        "CONTENT",
                        "USER",
                        LocalDateTime.now(),
                    ),
                ),
            ),
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/v1/problems/{problemId}/posts", "1")

        // then
        result.then().statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "posts/get",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("problemId").description("문제 id"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data[].id")
                            .type(JsonFieldType.NUMBER).description("글 ID"),
                        PayloadDocumentation.fieldWithPath("data[].content")
                            .type(JsonFieldType.STRING).description("글 내용"),
                        PayloadDocumentation.fieldWithPath("data[].username")
                            .type(JsonFieldType.STRING).description("작성자"),
                        PayloadDocumentation.fieldWithPath("data[].likeCount")
                            .type(JsonFieldType.NUMBER).description("좋아요 수"),
                        PayloadDocumentation.fieldWithPath("data[].isLiked")
                            .type(JsonFieldType.BOOLEAN).description("좋아요 여부"),
                        PayloadDocumentation.fieldWithPath("data[].comments[].id")
                            .type(JsonFieldType.NUMBER).description("댓글 ID"),
                        PayloadDocumentation.fieldWithPath("data[].comments[].content")
                            .type(JsonFieldType.STRING).description("댓글 내용"),
                        PayloadDocumentation.fieldWithPath("data[].comments[].username")
                            .type(JsonFieldType.STRING).description("댓글 작성자"),
                        PayloadDocumentation.fieldWithPath("data[].comments[].createdAt")
                            .type(JsonFieldType.STRING).description("댓글 생성일시"),
                    ),
                ),
            )
    }

    @Test
    fun `글 좋아요`() {
        // given
        justRun { postService.like(any(), any()) }

        // when
        val result = mockMvc.request(Method.POST, "/api/v1/posts/{postId}/like", "1")

        // then
        result.then().statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "posts/like",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("postId").description("글 id"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                    ),
                ),
            )
    }
}
