package io.csbroker.apiserver.model

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

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val problemTags: MutableSet<ProblemTag> = mutableSetOf(),

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val gradingHistory: MutableList<GradingHistory> = mutableListOf(),

    @OneToMany(mappedBy = "problem")
    val problemSetMapping: MutableList<ProblemSetMapping> = mutableListOf(),

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val problemLike: MutableList<ProblemLike> = mutableListOf(),

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
}
