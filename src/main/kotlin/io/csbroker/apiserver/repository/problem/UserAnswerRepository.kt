package io.csbroker.apiserver.repository.problem

import io.csbroker.apiserver.model.UserAnswer
import org.springframework.data.jpa.repository.JpaRepository

interface UserAnswerRepository : JpaRepository<UserAnswer, Long>
