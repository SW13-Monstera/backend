package com.csbroker.apiserver.dto.user

import com.csbroker.apiserver.common.enums.GradingStandardType
import com.csbroker.apiserver.model.GradingStandard

data class GradingStandardResponseDto(
    val id: Long,
    val content: String,
    val score: Double,
    val type: GradingStandardType
) {
    companion object {
        fun fromGradingStandard(gradingStandard: GradingStandard): GradingStandardResponseDto {
            return GradingStandardResponseDto(
                id = gradingStandard.id!!,
                content = gradingStandard.content,
                score = gradingStandard.score,
                type = gradingStandard.type
            )
        }
    }
}
