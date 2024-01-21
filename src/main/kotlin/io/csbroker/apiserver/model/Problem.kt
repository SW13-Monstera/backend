package io.csbroker.apiserver.model

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.GradingHistoryStats
import io.csbroker.apiserver.dto.problem.ProblemResponseDto
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "problem")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
abstract class Problem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id")
    var id: Long = 0,

    @Column(name = "problem_title", columnDefinition = "VARCHAR(50)")
    var title: String,

    @Column(name = "problem_description", columnDefinition = "LONGTEXT")
    var description: String,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "is_gradable")
    var isGradable: Boolean = true,

    @Column(name = "dtype", insertable = false, updatable = false)
    var dtype: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val creator: User,

    @Column(name = "score")
    var score: Double,

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val problemTags: MutableSet<ProblemTag> = mutableSetOf(),

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val gradingHistory: MutableList<GradingHistory> = mutableListOf(),

    @OneToMany(mappedBy = "problem")
    val problemSetMapping: MutableList<ProblemSetMapping> = mutableListOf(),

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val problemBookmark: MutableList<ProblemBookmark> = mutableListOf(),
) : BaseEntity() {
    fun toProblemResponseDto(gradingHistoryStats: GradingHistoryStats?): ProblemResponseDto {
        val tags = problemTags.map {
            it.tag
        }.map {
            it.name
        }

        return ProblemResponseDto(
            id,
            title,
            tags,
            gradingHistoryStats?.avgScore,
            gradingHistoryStats?.totalSolved ?: 0,
            dtype,
        )
    }

    fun addTag(tag: Tag) {
        val problemTag = ProblemTag(problem = this, tag = tag)
        if (problemTags.map { it.tag.name }.contains(tag.name)) {
            throw ConditionConflictException(ErrorCode.TAG_DUPLICATED, "해당 태그가 이미 존재합니다. tagName: ${tag.name}")
        }
        problemTags.add(problemTag)
    }

    fun addTags(tags: List<Tag>) {
        val newProblemTags = tags.map { ProblemTag(problem = this, tag = it)}
        val existProblemTags = problemTags.map { it.tag.name }.intersect(tags.map { it.name }.toSet())
        if (existProblemTags.isNotEmpty()) {
            throw ConditionConflictException(ErrorCode.TAG_DUPLICATED, "해당 태그가 이미 존재합니다. tagName: $existProblemTags")
        }
        problemTags.addAll(newProblemTags)
    }


    fun deleteTag(tag: Tag) {
        val problemTag = problemTags.find {
            it.tag.name == tag.name
        } ?: throw EntityNotFoundException("해당 태그가 존재하지 않습니다. tagName: ${tag.name}")

        problemTags.remove(problemTag)
    }

}
