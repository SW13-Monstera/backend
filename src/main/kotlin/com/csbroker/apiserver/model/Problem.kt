package com.csbroker.apiserver.model

import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "problem")
class Problem(
    @Id
    @GeneratedValue
    @Column(name = "problem_id")
    var id: UUID? = null,

    @Column(name = "problem_title")
    var title: String,

    @Column(name = "problem_description")
    var description: String,

    @Column(name = "standard_answer")
    var answer: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val creator: User,

    @OneToMany(mappedBy = "problem")
    val problemTags: MutableList<ProblemTag> = mutableListOf(),
) {
}