package com.csbroker.apiserver.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

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
    val gradingStandard: GradingStandard
)
