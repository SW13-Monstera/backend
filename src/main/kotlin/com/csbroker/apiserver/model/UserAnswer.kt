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
@Table(name = "user_answer")
class UserAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_answer_id")
    val id: Long? = null,

    @Column(name = "answer")
    val answer: String,

    @Column(name = "is_labeled")
    val isLabeled: Boolean = false,

    @Column(name = "is_validated")
    val isValidated: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    var problem: LongProblem,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    var assignedUser: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validator_id")
    var validatingUser: User? = null
)
