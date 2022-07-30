package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.UserAnswerUpsertDto
import com.csbroker.apiserver.repository.UserAnswerRepository
import org.springframework.stereotype.Service

@Service
class UserAnswerServiceImpl(
    private val userAnswerRepository: UserAnswerRepository
) : UserAnswerService {
    override fun createUserAnswers(userAnswers: List<UserAnswerUpsertDto>): Int {
        this.userAnswerRepository.batchInsert(userAnswers)
        return userAnswers.size
    }
}
