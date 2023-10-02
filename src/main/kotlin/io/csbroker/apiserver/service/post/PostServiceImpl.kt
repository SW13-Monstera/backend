package io.csbroker.apiserver.service.post

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.controller.v1.post.response.CommentResponseDto
import io.csbroker.apiserver.controller.v1.post.response.PostResponseDto
import io.csbroker.apiserver.model.Post
import io.csbroker.apiserver.model.PostLike
import io.csbroker.apiserver.repository.post.CommentRepository
import io.csbroker.apiserver.repository.post.PostLikeRepository
import io.csbroker.apiserver.repository.post.PostRepository
import io.csbroker.apiserver.repository.problem.ProblemRepository
import io.csbroker.apiserver.repository.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PostServiceImpl(
    private val problemRepository: ProblemRepository,
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
) : PostService {
    override fun findByProblemId(problemId: Long, email: String?): List<PostResponseDto> {
        val problem = problemRepository.findByIdOrNull(problemId) ?: throw EntityNotFoundException(
            "${problemId}번 문제는 존재하지 않습니다",
        )
        val posts = postRepository.findAllByProblem(problem)
        val comments = commentRepository.findAllByPostIn(posts)
        val postLikes = postLikeRepository.findAllByPostIn(posts)
        return posts.map {
            PostResponseDto(
                it,
                likeCount = postLikes.count { postLike -> postLike.post == it }.toLong(),
                isLiked = postLikes.any { postLike -> postLike.post == it && postLike.user.email == email },
                comments = comments.filter { comment -> comment.post == it }.map { comment ->
                    CommentResponseDto(
                        comment,
                    )
                },
            )
        }
    }

    @Transactional
    override fun create(problemId: Long, content: String, email: String): Long {
        val user = userRepository.findByEmail(email) ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")
        val problem = problemRepository.findByIdOrNull(problemId) ?: throw EntityNotFoundException(
            "${problemId}번 문제는 존재하지 않는 문제입니다",
        )
        val post = postRepository.save(Post(content = content, problem = problem, user = user))
        return post.id!!
    }

    @Transactional
    override fun like(id: Long, email: String) {
        val post = postRepository.findByIdOrNull(id) ?: throw EntityNotFoundException("${id}번 답변은 존재하지 않는 답변입니다")
        val user = userRepository.findByEmail(email) ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")
        val postLike = postLikeRepository.findByPostAndUser(post, user)
        if (postLike == null) {
            postLikeRepository.save(PostLike(post = post, user = user))
        } else {
            postLikeRepository.delete(postLike)
        }
    }

    @Transactional
    override fun deleteById(id: Long, email: String) {
        val user = userRepository.findByEmail(email) ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")
        val post = postRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 답변은 존재하지 않는 답변입니다")
        if (post.user != user) {
            throw UnAuthorizedException(ErrorCode.FORBIDDEN, "해당 답변을 삭제할 권한이 없습니다")
        }
        postRepository.delete(post)
    }
}
