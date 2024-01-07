package io.csbroker.apiserver.repository.post

import io.csbroker.apiserver.model.Comment
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long>
