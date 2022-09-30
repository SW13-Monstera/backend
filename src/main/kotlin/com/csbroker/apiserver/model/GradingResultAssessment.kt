package com.csbroker.apiserver.model

import com.csbroker.apiserver.common.enums.AssessmentType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "grading_result_assessment")
class GradingResultAssessment(
    @Id
    @GeneratedValue
    @Column(name = "grading_result_assessment_id")
    val id: Long? = null,

    @OneToOne(mappedBy = "gradingResultAssessment", fetch = FetchType.LAZY)
    val gradingHistory: GradingHistory,

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    var type: AssessmentType,

    @Column(name = "assessment_content", columnDefinition = "VARCHAR(150)")
    val content: String
)
