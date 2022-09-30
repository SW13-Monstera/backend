package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.GradingResultAssessment
import org.springframework.data.jpa.repository.JpaRepository

interface GradingResultAssessmentRepository : JpaRepository<GradingResultAssessment, Long>
