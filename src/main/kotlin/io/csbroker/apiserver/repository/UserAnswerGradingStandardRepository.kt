package io.csbroker.apiserver.repository

import io.csbroker.apiserver.model.UserAnswerGradingStandard
import org.springframework.data.jpa.repository.JpaRepository

interface UserAnswerGradingStandardRepository :
    JpaRepository<UserAnswerGradingStandard, Long>,
    UserAnswerGradingStandardRepositoryCustom {
    fun deleteAllByUserAnswerId(userAnswerId: Long)
}
