package io.csbroker.apiserver.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "grading_standard")
class GradingStandard(  // Todo : 제거 예정
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grading_standard_id")
    val id: Long = 0,

    @Column(name = "content")
    var content: String,

    @Column(name = "score")
    var score: Double,

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    var type: GradingStandardType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    var problem: LongProblem,

    @OneToMany(mappedBy = "gradingStandard", cascade = [CascadeType.ALL])
    var userAnswerGradingStandards: MutableList<UserAnswerGradingStandard> = mutableListOf(),
) : BaseEntity()
