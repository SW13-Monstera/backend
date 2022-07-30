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
@Table(name = "choice")
class Choice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "choice_id")
    val id: Long? = null,

    @Column(name = "content")
    var content: String,

    @Column(name = "is_answer")
    var isAnswer: Boolean,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    var multipleChoiceProblem: MultipleChoiceProblem
)
