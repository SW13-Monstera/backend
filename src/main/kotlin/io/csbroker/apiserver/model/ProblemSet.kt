package io.csbroker.apiserver.model

import io.csbroker.apiserver.dto.problem.GradingHistoryStats
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetDetailResponseDto
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetResponseDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "problem_set")
class ProblemSet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_set_id")
    val id: Long = 0,

    @Column(name = "problem_set_name")
    var name: String,

    @Column(name = "problem_set_description")
    var description: String,

    @OneToMany(mappedBy = "problemSet")
    val problemSetMapping: List<ProblemSetMapping> = mutableListOf(),
) {
    fun toProblemSetResponseDto(): ProblemSetResponseDto {
        return ProblemSetResponseDto(
            id,
            problemSetMapping.size,
            name,
            description,
        )
    }

    fun toProblemSetDetailResponseDto(): ProblemSetDetailResponseDto {
        return ProblemSetDetailResponseDto(
            id,
            problemSetMapping.map {
                it.problem.toProblemResponseDto(GradingHistoryStats.toGradingHistoryStats(it.problem.gradingHistory))
            },
            name,
            description,
        )
    }

    fun updateContents(name: String, description: String) {
        this.name = name
        this.description = description
    }
}
