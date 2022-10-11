package io.csbroker.apiserver.repository

interface UserAnswerGradingStandardRepositoryCustom {
    fun batchInsert(userAnswerId: Long, gradingStandardIds: List<Long>)
}
