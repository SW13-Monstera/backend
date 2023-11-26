package io.csbroker.apiserver.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "user_answer_grading_standard")
class UserAnswerGradingStandard(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_answer_grading_standard_id")
    val userAnswerGradingStandardId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_answer_id")
    val userAnswer: UserAnswer,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grading_standard_id")
    val gradingStandard: GradingStandard,
)
