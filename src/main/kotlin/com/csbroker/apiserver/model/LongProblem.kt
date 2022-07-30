package com.csbroker.apiserver.model

import com.csbroker.apiserver.dto.problem.LongProblemUpsertRequestDto
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "long_problem")
@DiscriminatorValue("long")
class LongProblem(
    title: String,
    description: String,
    creator: User,

    @Column(name = "is_gradable")
    var isGradable: Boolean = false,

    @Column(name = "standard_answer", columnDefinition = "VARCHAR(300)")
    var standardAnswer: String,

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL])
    var gradingStandards: MutableList<GradingStandard> = mutableListOf(),

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL])
    var userAnswers: MutableList<UserAnswer> = mutableListOf()
) : Problem(title = title, description = description, creator = creator) {
    fun addGradingStandards(gradingStandards: List<GradingStandard>) {
        this.gradingStandards.addAll(gradingStandards)
    }

    fun updateFromDto(upsertRequestDto: LongProblemUpsertRequestDto) {
        this.title = upsertRequestDto.title
        this.description = upsertRequestDto.description
        this.standardAnswer = upsertRequestDto.standardAnswer
        this.isGradable = false
    }
}
