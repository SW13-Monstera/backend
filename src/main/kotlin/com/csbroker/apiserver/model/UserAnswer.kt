package com.csbroker.apiserver.model

import com.csbroker.apiserver.common.enums.GradingStandardType
import com.csbroker.apiserver.dto.useranswer.UserAnswerSearchResponseDto
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "user_answer")
class UserAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_answer_id")
    val id: Long? = null,

    @Column(name = "answer", columnDefinition = "VARCHAR(300)")
    val answer: String,

    @Column(name = "is_labeled")
    var isLabeled: Boolean = false,

    @Column(name = "is_validated")
    var isValidated: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    var problem: LongProblem,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    var assignedUser: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validator_id")
    var validatingUser: User? = null,

    @OneToMany(mappedBy = "userAnswer", cascade = [CascadeType.ALL])
    var userAnswerGradingStandards: MutableList<UserAnswerGradingStandard> = mutableListOf(),
) : BaseEntity() {
    fun getKeywordScore(): Double {
        return this.userAnswerGradingStandards.map {
            it.gradingStandard
        }.filter {
            it.type == GradingStandardType.KEYWORD
        }.sumOf {
            it.score
        }
    }

    fun getPromptScore(): Double {
        return this.userAnswerGradingStandards.map {
            it.gradingStandard
        }.filter {
            it.type == GradingStandardType.PROMPT
        }.sumOf {
            it.score
        }
    }

    fun toUserAnswerDataDto(): UserAnswerSearchResponseDto.UserAnswerDataDto {
        return UserAnswerSearchResponseDto.UserAnswerDataDto(
            this.id!!,
            this.problem.title,
            this.assignedUser?.username,
            this.validatingUser?.username,
            this.updatedAt!!,
            this.isLabeled,
            this.isValidated
        )
    }
}
