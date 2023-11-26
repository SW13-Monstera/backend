package io.csbroker.apiserver.model

import io.csbroker.apiserver.common.enums.AssessmentType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "grading_result_assessment")
class GradingResultAssessment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grading_result_assessment_id")
    val id: Long = 0,

    @OneToOne(mappedBy = "gradingResultAssessment", fetch = FetchType.LAZY)
    val gradingHistory: GradingHistory,

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    var type: AssessmentType,

    @Column(name = "assessment_content", columnDefinition = "VARCHAR(150)")
    val content: String,
)
