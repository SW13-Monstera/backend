package io.csbroker.apiserver.controller.v1.post

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.csbroker.apiserver.common.enums.LikeType
import io.csbroker.apiserver.controller.IntegrationTest
import io.csbroker.apiserver.controller.v1.post.request.CommentCreateRequestDto
import io.csbroker.apiserver.model.Comment
import io.csbroker.apiserver.model.Like
import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.Post
import io.kotest.matchers.shouldBe
import jakarta.persistence.NoResultException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CommentControllerIntegrationTest : IntegrationTest() {

    private val baseUrl = "/api/v1/comments"
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun createCommentTest() {
        // given
        val problem = save(LongProblem(title = "title", description = "description", creator = defaultUser))
        val post = save(Post(problem = problem, user = defaultUser, content = "content"))

        // when
        val response = request(
            method = HttpMethod.POST,
            url = baseUrl,
            body = CommentCreateRequestDto(postId = post.id, content = "comment content"),
        )

        // then
        response.andExpect(status().isOk)
            .andDo {
                val commentId = objectMapper.readTree(it.response.contentAsString)
                    .get("data")
                    .toString()
                    .toLong()
                val comment = findOne<Comment>(
                    "SELECT c FROM Comment c WHERE c.id = :id",
                    mapOf("id" to commentId),
                )
                comment.content shouldBe "comment content"
            }
    }

    @Test
    fun deleteCommentByIdTest() {
        // given
        val problem = save(LongProblem(title = "title", description = "description", creator = defaultUser))
        val post = save(Post(problem = problem, user = defaultUser, content = "content"))
        val comment = save(Comment(post = post, user = defaultUser, content = "comment content"))

        // when
        val response = request(
            method = HttpMethod.DELETE,
            url = baseUrl + "/${comment.id}",
        )

        // then
        response.andExpect(status().isOk)
        assertThrows<NoResultException> {
            findOne<Comment>("SELECT c FROM Comment c WHERE c.id = :id", mapOf("id" to comment.id))
        }
    }

    @Test
    fun likeCommentTest() {
        // given
        val problem = save(LongProblem(title = "title", description = "description", creator = defaultUser))
        val post = save(Post(problem = problem, user = defaultUser, content = "content"))
        val comment = save(Comment(post = post, user = defaultUser, content = "comment content"))

        // when
        val likeResponse = request(
            method = HttpMethod.POST,
            url = baseUrl + "/${comment.id}/like",
        )
        // then
        likeResponse.andExpect(status().isOk)
            .andDo {
                val likes = findAll<Like>(
                    "SELECT l FROM Like l WHERE l.user.id = :userId AND l.targetId = :targetId AND l.type = :type",
                    mapOf("userId" to defaultUser.id!!, "targetId" to comment.id, "type" to LikeType.COMMENT),
                )
                likes.size shouldBe 1
                likes.first().targetId shouldBe comment.id
            }

        // when
        val notLikeResponse = request(
            method = HttpMethod.POST,
            url = baseUrl + "/${comment.id}/like",
        )

        // then
        notLikeResponse.andExpect(status().isOk)
            .andDo {
                defaultUser
                assertThrows<NoResultException> {
                    findOne<Like>(
                        "SELECT l FROM Like l WHERE l.user.id = :userId AND l.targetId = :targetId AND l.type = :type",
                        mapOf("userId" to defaultUser.id!!, "targetId" to comment.id, "type" to LikeType.COMMENT),
                    )
                }
            }
    }
}
