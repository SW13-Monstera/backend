package io.csbroker.apiserver.repository

import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.StandardAnswer
import org.springframework.data.jpa.repository.JpaRepository

interface StandardAnswerRepository : JpaRepository<StandardAnswer, Long> {
    fun findAllByLongProblem(longProblem: LongProblem): List<StandardAnswer>
    fun deleteAllByLongProblem(longProblem: LongProblem)
}
