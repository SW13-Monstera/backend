package io.csbroker.apiserver.model

import io.csbroker.apiserver.dto.problem.GradingHistoryStats
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetDetailResponseDto
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetResponseDto
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "problem_set")
class ProblemSet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_set_id")
    val id: Long? = null,

    @Column(name = "problem_set_name")
    var name: String,

    @Column(name = "problem_set_description")
    var description: String,

    @OneToMany
    val problemSetMapping: List<ProblemSetMapping> = mutableListOf()
) {
    fun toProblemSetResponseDto(): ProblemSetResponseDto {
        return ProblemSetResponseDto(
            id!!,
            problemSetMapping.size,
            name,
            description,
        )
    }

    fun toProblemSetDetailResponseDto(): ProblemSetDetailResponseDto {
        return ProblemSetDetailResponseDto(
            id!!,
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
