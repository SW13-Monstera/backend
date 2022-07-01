package com.csbroker.apiserver.model

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
) : BaseEntity()
