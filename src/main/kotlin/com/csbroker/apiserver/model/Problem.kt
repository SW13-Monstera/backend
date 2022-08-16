package com.csbroker.apiserver.model

import com.csbroker.apiserver.dto.problem.ProblemResponseDto
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.DiscriminatorColumn
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "problem")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
abstract class Problem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id")
    val id: Long? = null,

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

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL])
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
            totalSolved,
            this.dtype
        )
    }
}
