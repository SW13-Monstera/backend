package io.csbroker.apiserver.repository.problem

import io.csbroker.apiserver.model.Problem
import io.csbroker.apiserver.model.ProblemLike
import io.csbroker.apiserver.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface ProblemLikeRepository : JpaRepository<ProblemLike, Long> {
    fun findByUserAndProblem(user: User, problem: Problem): ProblemLike?
}
