package com.csbroker.apiserver.model

import com.csbroker.apiserver.dto.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.ProblemResponseDto
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "problem")
class Problem(
    @Id
    @GeneratedValue
    @Column(name = "problem_id")
    val id: UUID? = null,

    @Column(name = "problem_title", columnDefinition = "VARCHAR(50)")
    var title: String,

    @Column(name = "problem_description", columnDefinition = "LONGTEXT")
    var description: String,

    @Column(name = "standard_answer", columnDefinition = "VARCHAR(300)")
    var answer: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val creator: User,

    @OneToMany(mappedBy = "problem")
    val problemTags: MutableList<ProblemTag> = mutableListOf(),

    @OneToMany(mappedBy = "problem")
    val gradingHistory: MutableList<GradingHistory> = mutableListOf()
) : BaseEntity() {
    fun toProblemResponseDto(): ProblemResponseDto {
        val tags = this.problemTags.map {
            it.tag
        }.map {
            it.name
        }

        val avgScore = this.gradingHistory.map {
            it.score
        }.average()

        val totalSolved = this.gradingHistory.map {
            it.user.username
        }.distinct().size

        return ProblemResponseDto(
            this.title,
            tags,
            avgScore,
            totalSolved
        )
    }

    fun toProblemDetailResponseDto(): ProblemDetailResponseDto {
        val tags = this.problemTags.map {
            it.tag
        }.map {
            it.name
        }

        val scoreList = this.gradingHistory.map {
            it.score
        }.toList().sorted()

        val totalSolved = this.gradingHistory.map {
            it.user.username
        }.distinct().size

        return ProblemDetailResponseDto(
            this.title,
            tags,
            this.description,
            scoreList.average(),
            scoreList.first(),
            scoreList.last(),
            totalSolved
        )
    }
}
