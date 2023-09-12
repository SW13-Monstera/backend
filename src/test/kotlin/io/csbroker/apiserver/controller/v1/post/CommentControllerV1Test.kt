package io.csbroker.apiserver.controller.v1.post

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.service.post.CommentService
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

class CommentControllerV1Test : RestDocsTest() {
    private lateinit var commentService: CommentService
    private lateinit var mockMvc: MockMvcRequestSpecification

    @BeforeEach
    fun setUp() {
        commentService = mockk()
        mockMvc = mockMvc(
            CommentControllerV1(commentService),
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
}
