package io.csbroker.apiserver.dto.problem.grade

import io.csbroker.apiserver.common.enums.AssessmentType
import io.csbroker.apiserver.model.GradingHistory
import io.csbroker.apiserver.model.GradingResultAssessment

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
