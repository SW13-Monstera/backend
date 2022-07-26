package com.csbroker.apiserver.model

import com.csbroker.apiserver.common.enums.GradingStandardType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "grading_standard")
class GradingStandard(
    @Id
    @GeneratedValue
    @Column(name = "grading_standard_id")
    val id: Long? = null,

    @Column(name = "content")
    var content: String,

    @Column(name = "score")
    var score: Double,

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    var type: GradingStandardType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    var problem: LongProblem
) : BaseEntity()
