package io.csbroker.apiserver.service.post

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.model.Comment
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.post.CommentRepository
import io.csbroker.apiserver.repository.post.PostRepository
import io.csbroker.apiserver.repository.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import jakarta.persistence.EntityNotFoundException

@Service
@Transactional(readOnly = true)
class CommentServiceImpl(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
) : CommentService {
    @Transactional
    override fun create(postId: Long, content: String, user: User): Long {
        val post = postRepository.findByIdOrNull(postId) ?: throw EntityNotFoundException(
            "${postId}번 답변은 존재하지 않는 답변입니다",
        )
        val comment = commentRepository.save(Comment(content = content, post = post, user = user))
        return comment.id
    }

    @Transactional
    override fun deleteById(id: Long, user: User) {
        val comment = commentRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 답변은 존재하지 않는 답변입니다")
        if (comment.user != user) {
            throw UnAuthorizedException(ErrorCode.FORBIDDEN, "해당 답변을 삭제할 권한이 없습니다")
        }
        commentRepository.delete(comment)
    }
}
