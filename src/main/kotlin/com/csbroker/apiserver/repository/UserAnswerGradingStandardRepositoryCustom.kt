package com.csbroker.apiserver.repository

interface UserAnswerGradingStandardRepositoryCustom {
    fun batchInsert(userAnswerId: Long, gradingStandardIds: List<Long>)
}
