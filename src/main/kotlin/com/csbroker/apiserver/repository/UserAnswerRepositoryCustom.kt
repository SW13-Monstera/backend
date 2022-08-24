package com.csbroker.apiserver.repository

import com.csbroker.apiserver.dto.useranswer.UserAnswerUpsertDto
import com.csbroker.apiserver.model.UserAnswer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserAnswerRepositoryCustom {
    fun batchInsert(userAnswers: List<UserAnswerUpsertDto>)
    fun findUserAnswersByQuery(
        id: Long?,
        assignedBy: String?,
        validatedBy: String?,
        problemTitle: String?,
        answer: String?,
        isLabeled: Boolean?,
        isValidated: Boolean?,
        pageable: Pageable
    ): Page<UserAnswer>
}
