package io.csbroker.apiserver.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "grading_history")
class GradingHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grading_history_id")
    val gradingHistoryId: Long = 0,

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
    var gradingResultAssessment: GradingResultAssessment? = null,
) : BaseEntity()
