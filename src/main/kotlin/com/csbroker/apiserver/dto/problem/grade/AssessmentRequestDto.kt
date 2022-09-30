package com.csbroker.apiserver.dto.problem.grade

import com.csbroker.apiserver.common.enums.AssessmentType
import com.csbroker.apiserver.model.GradingHistory
import com.csbroker.apiserver.model.GradingResultAssessment

data class AssessmentRequestDto(
    val assessmentType: AssessmentType,
    val content: String = ""
) {
    fun toGradingResultAssessment(gradingHistory: GradingHistory): GradingResultAssessment {
        return GradingResultAssessment(
            gradingHistory = gradingHistory,
            type = assessmentType,
            content = content
        )
    }
}
