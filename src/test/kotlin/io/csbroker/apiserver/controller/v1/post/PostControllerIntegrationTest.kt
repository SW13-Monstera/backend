package io.csbroker.apiserver.controller.v1.post

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.csbroker.apiserver.common.enums.LikeType
import io.csbroker.apiserver.controller.IntegrationTest
import io.csbroker.apiserver.controller.v1.post.request.PostCreateRequestDto
import io.csbroker.apiserver.controller.v1.post.response.PostResponseDto
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

class PostControllerIntegrationTest : IntegrationTest() {

    private val baseUrl = "/api/v1/posts"
    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Test
    fun createPostTest() {
        // given
        val problem = save(LongProblem("title", "description", defaultUser))

        // when
        val response = request(HttpMethod.POST, baseUrl, PostCreateRequestDto(problem.id, "post content"))

        // then
        response.andExpect(status().isOk)
            .andDo {
                val postId = objectMapper.readTree(it.response.contentAsString).get("data").toString().toLong()
                val post = findOne<Post>("SELECT p FROM Post p WHERE p.id = :id", mapOf("id" to postId))
                post.content shouldBe "post content"
            }
    }

    @Test
    fun deletePostTest() {
        // given
        val problem = save(LongProblem("title", "description", defaultUser))
        val post = save(Post(problem = problem, user = defaultUser, content = "post content"))

        // when
        val response = request(
            method = HttpMethod.DELETE,
            url = baseUrl + "/${post.id}",
        )

        response.andExpect(status().isOk)
            .andDo {
                assertThrows<NoResultException> {
                    findOne<Post>("SELECT p FROM Post p WHERE p.id = :id", mapOf("id" to post.id))
                }
            }
    }

    @Test
    fun findAllByProblemIdTest() {
        // given
        val problem = save(LongProblem("title", "description", defaultUser))
        val posts = (1..10).map { save(Post(problem = problem, user = defaultUser, content = "post content $it")) }

        // when
        val response = request(
            method = HttpMethod.GET,
            url = "/api/v1/problems/${problem.id}/posts",
        )

        response.andExpect(status().isOk)
            .andDo {
                val dataString = objectMapper.readTree(it.response.contentAsString).get("data").toString()
                val postResponses = objectMapper.readValue(dataString, Array<PostResponseDto>::class.java)
                postResponses.size shouldBe posts.size
            }
    }

    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @Test
    fun `findAllByProblemIdTest - response with like`() {
        // given
        val problem = save(LongProblem("title", "description", defaultUser))
        val post = save(Post(problem = problem, user = defaultUser, content = "post content"))
        save(Comment(post = post, user = defaultUser, content = "comment content"))
        save(Like(user = defaultUser, targetId = post.id, type = LikeType.POST))
        val expectPostLikeCount = 1L

        // when
        val response = request(
            method = HttpMethod.GET,
            url = "/api/v1/problems/${problem.id}/posts",
        )

        response.andExpect(status().isOk)
            .andDo {
                val dataString = objectMapper.readTree(it.response.contentAsString).get("data").toString()
                print(dataString)
                val postResponses = objectMapper.readValue(dataString, Array<PostResponseDto>::class.java)

                postResponses.size shouldBe 1
                postResponses.first().isLiked shouldBe true
                postResponses.first().likeCount shouldBe expectPostLikeCount
                !postResponses.first().comments.first().isLiked shouldBe true
            }
    }

    @Test
    fun likePostTest() {
        // given
        val problem = save(LongProblem("title", "description", defaultUser))
        val post = save(Post(problem = problem, user = defaultUser, content = "post content"))

        // when
        val likeResponse = request(
            method = HttpMethod.POST,
            url = baseUrl + "/${post.id}/like",
        )

        // then
        likeResponse.andExpect(status().isOk)
            .andDo {
                val likes = findAll<Like>(
                    "SELECT l FROM Like l WHERE l.user.id = :userId AND l.targetId = :targetId AND l.type = :type",
                    mapOf("userId" to defaultUser.id!!, "targetId" to post.id, "type" to LikeType.POST),
                )
                likes.size shouldBe 1
                likes.first().targetId shouldBe post.id
            }

        // when
        val notLikeResponse = request(
            method = HttpMethod.POST,
            url = baseUrl + "/${post.id}/like",
        )

        // then
        notLikeResponse.andExpect(status().isOk)
            .andDo {
                assertThrows<NoResultException> {
                    findOne<Like>(
                        "SELECT l FROM Like l WHERE l.user.id = :userId AND l.targetId = :targetId AND l.type = :type",
                        mapOf("userId" to defaultUser.id!!, "targetId" to post.id, "type" to LikeType.POST),
                    )
                }
            }
    }
}
