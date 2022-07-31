package com.csbroker.apiserver.model

import com.csbroker.apiserver.dto.problem.ShortProblemResponseDto
import com.csbroker.apiserver.dto.problem.ShortProblemUpsertRequestDto
import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "short_problem")
@DiscriminatorValue("short")
class ShortProblem(
    title: String,
    description: String,
    creator: User,

    @Column(name = "answer")
    var answer: String,

    @Column(name = "score")
    var score: Double
) : Problem(title = title, description = description, creator = creator) {
    fun updateFromDto(upsertRequestDto: ShortProblemUpsertRequestDto) {
        this.title = upsertRequestDto.title
        this.description = upsertRequestDto.description
        this.answer = upsertRequestDto.answer
    }

    fun toShortProblemResponseDto(): ShortProblemResponseDto {
        return ShortProblemResponseDto(
            this.id!!,
            this.title,
            this.description,
            this.problemTags.map { it.tag.name },
            this.answer,
            this.score
        )
    }
}
