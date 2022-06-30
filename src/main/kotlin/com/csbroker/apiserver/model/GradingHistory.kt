package com.csbroker.apiserver.model

import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "grading_history")
class GradingHistory(
    @Id
    @GeneratedValue
    @Column(name = "grading_history_id")
    var gradingHistoryId: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    val problem: Problem,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(name = "user_answer")
    val userAnswer: String,

    @Column(name = "score")
    val score: Float
) {
}