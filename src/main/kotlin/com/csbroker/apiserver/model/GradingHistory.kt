package com.csbroker.apiserver.model

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "grading_history")
class GradingHistory(
    @Id
    @GeneratedValue
    @Column(name = "grading_history_id")
    val gradingHistoryId: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    val problem: Problem,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(name = "user_answer", columnDefinition = "VARCHAR(300)")
    val userAnswer: String,

    @Column(name = "score")
    val score: Float
) : BaseEntity()
