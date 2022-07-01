package com.csbroker.apiserver.model

import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "problem_tag")
class ProblemTag(
    @Id
    @GeneratedValue
    @Column(name = "problem_tag_id")
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    val problem: Problem,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    val tag: Tag
)
