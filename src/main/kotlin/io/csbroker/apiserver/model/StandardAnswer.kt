package io.csbroker.apiserver.model

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
@Table(name = "standard_answer")
class StandardAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "standard_answer_id")
    val id: Long = 0,

    @Column(name = "content", columnDefinition = "LONGTEXT")
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "long_problem_id")
    val longProblem: LongProblem,
)
