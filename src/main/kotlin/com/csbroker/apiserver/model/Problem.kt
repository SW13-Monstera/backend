package com.csbroker.apiserver.model

import com.csbroker.apiserver.dto.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.ProblemResponseDto
import org.hibernate.annotations.GenericGenerator
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
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "problem_id", columnDefinition = "BINARY(16)")
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
    val problemTags: MutableSet<ProblemTag> = mutableSetOf(),

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
        }.average().let {
            if (it.isNaN()) {
                null
            } else {
                it
            }
        }

        val totalSolved = this.gradingHistory.map {
            it.user.username
        }.distinct().size

        return ProblemResponseDto(
            this.id!!,
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
            this.id!!,
            this.title,
            tags,
            this.description,
            if (scoreList.isEmpty()) null else scoreList.average(),
            if (scoreList.isEmpty()) null else scoreList.first(),
            if (scoreList.isEmpty()) null else scoreList.last(),
            totalSolved
        )
    }
}
