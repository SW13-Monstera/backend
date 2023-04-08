package io.csbroker.apiserver.repository.problem

import io.csbroker.apiserver.model.GradingResultAssessment
import org.springframework.data.jpa.repository.JpaRepository

interface GradingResultAssessmentRepository : JpaRepository<GradingResultAssessment, Long>
