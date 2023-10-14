package io.csbroker.apiserver.repository.problem

import io.csbroker.apiserver.model.Problem
import io.csbroker.apiserver.model.ProblemBookmark
import io.csbroker.apiserver.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface ProblemBookmarkRepository : JpaRepository<ProblemBookmark, Long> {
    fun findByUserAndProblem(user: User, problem: Problem): ProblemBookmark?
}
