package io.csbroker.apiserver.repository.problem

interface UserAnswerGradingStandardRepositoryCustom {
    fun batchInsert(userAnswerId: Long, gradingStandardIds: List<Long>)
}
