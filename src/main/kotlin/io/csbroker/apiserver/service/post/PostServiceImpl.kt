package io.csbroker.apiserver.service.post

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.enums.LikeType
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.controller.v1.post.response.CommentResponseDto
import io.csbroker.apiserver.controller.v1.post.response.PostResponseDto
import io.csbroker.apiserver.model.Comment
import io.csbroker.apiserver.model.Like
import io.csbroker.apiserver.model.Post
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.post.LikeRepository
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
    private val likeRepository: LikeRepository,
    private val userRepository: UserRepository,
) : PostService {
    override fun findByProblemId(problemId: Long, emailIfLogin: String?): List<PostResponseDto> {
        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw EntityNotFoundException("${problemId}번 문제는 존재하지 않습니다")

        val user = emailIfLogin?.let {
            userRepository.findByEmail(it)
                ?: throw EntityNotFoundException("$emailIfLogin 을 가진 유저는 존재하지 않습니다.")
        }
        val posts = postRepository.findAllByProblem(problem)
        val comments = posts.flatMap { it.comments }
        val postLikeMap = likeRepository.findAllByPostIdIn(posts.map { it.id })
            .groupBy { it.targetId }
        val commentLikeMap = likeRepository.findByCommentIdIn(comments.map { it.id })
            .groupBy { it.targetId }

        return posts.map {
            combineResponseDto(
                post = it,
                comments = comments,
                postLikes = postLikeMap[it.id] ?: emptyList(),
                commentLikeMap = commentLikeMap,
                user = user,
            )
        }
    }

    override fun findByPostId(postId: Long, emailIfLogin: String?): PostResponseDto {
        val post = postRepository.findByIdOrNull(postId)
            ?: throw EntityNotFoundException("id : $postId 게시글은 존재하지 않는 게시글입니다")
        val user = emailIfLogin?.let {
            userRepository.findByEmail(it)
                ?: throw EntityNotFoundException("$emailIfLogin 을 가진 유저는 존재하지 않습니다.")
        }

        val postLikes = likeRepository.findAllByPostIdIn(listOf(postId))
        val commentLikeMap = likeRepository.findByCommentIdIn(post.comments.map { it.id })
            .groupBy { it.targetId }

        return combineResponseDto(
            post = post,
            comments = post.comments,
            postLikes = postLikes,
            commentLikeMap = commentLikeMap,
            user = user,
        )
    }

    private fun combineResponseDto(
        post: Post,
        comments: List<Comment>,
        postLikes: List<Like>,
        commentLikeMap: Map<Long, List<Like>>,
        user: User?,
    ) = PostResponseDto(
        post,
        likeCount = postLikes.count(),
        isLiked = postLikes.any { like -> like.user == user },
        comments = comments.map { comment ->
            CommentResponseDto(
                comment = comment,
                likeCount = commentLikeMap[comment.id]?.count() ?: 0,
                isLiked = commentLikeMap[comment.id]?.any { like -> like.user == user } ?: false,
            )
        },
    )

    @Transactional
    override fun create(problemId: Long, content: String, user: User): Long {
        val problem = problemRepository.findByIdOrNull(problemId) ?: throw EntityNotFoundException(
            "${problemId}번 문제는 존재하지 않는 문제입니다",
        )
        val post = postRepository.save(Post(content = content, problem = problem, user = user))
        return post.id
    }

    @Transactional
    override fun like(id: Long, user: User) {
        val post = postRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 게시글은 존재하지 않습니다.")

        likeRepository.findByPostIdAndUser(post.id, user)?.let { likeRepository.delete(it) }
            ?: likeRepository.save(Like(targetId = post.id, user = user, type = LikeType.POST))
    }

    @Transactional
    override fun deleteById(id: Long, user: User) {
        val post = postRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 답변은 존재하지 않는 답변입니다")
        if (post.user != user) {
            throw UnAuthorizedException(ErrorCode.FORBIDDEN, "해당 답변을 삭제할 권한이 없습니다")
        }
        postRepository.delete(post)
    }
}
