package io.csbroker.apiserver.model

import io.csbroker.apiserver.common.enums.GradingStandardType
import io.csbroker.apiserver.dto.useranswer.UserAnswerSearchResponseDto.UserAnswerDataDto
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
        return userAnswerGradingStandards.map {
            it.gradingStandard
        }.filter {
            it.type == GradingStandardType.KEYWORD
        }.sumOf {
            it.score
        }
    }

    fun getContentScore(): Double {
        return userAnswerGradingStandards.map {
            it.gradingStandard
        }.filter {
            it.type == GradingStandardType.CONTENT
        }.sumOf {
            it.score
        }
    }

    fun toUserAnswerDataDto(): UserAnswerDataDto {
        return UserAnswerDataDto(
            id!!,
            problem.title,
            assignedUser?.username,
            validatingUser?.username,
            updatedAt!!,
            isLabeled,
            isValidated,
        )
    }
}
