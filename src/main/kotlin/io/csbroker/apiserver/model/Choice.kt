package io.csbroker.apiserver.model

import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.ChoiceResponseDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "choice")
class Choice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "choice_id")
    val id: Long = 0,

    @Column(name = "content")
    var content: String,

    @Column(name = "is_answer")
    var isAnswer: Boolean,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    var multipleChoiceProblem: MultipleChoiceProblem,
) {
    fun toChoiceResponseDto(): ChoiceResponseDto {
        return ChoiceResponseDto(
            id,
            content,
        )
    }
}
