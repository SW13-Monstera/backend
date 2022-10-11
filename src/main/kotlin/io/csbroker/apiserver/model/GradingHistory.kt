package io.csbroker.apiserver.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "grading_history")
class GradingHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grading_history_id")
    val gradingHistoryId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    val problem: Problem,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(name = "user_answer", columnDefinition = "VARCHAR(300)")
    val userAnswer: String,

    @Column(name = "score")
    val score: Double,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grading_result_assessment_id")
    var gradingResultAssessment: GradingResultAssessment? = null
) : BaseEntity()
