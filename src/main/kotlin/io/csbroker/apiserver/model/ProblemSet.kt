package io.csbroker.apiserver.model

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
    val name: String,

    @Column(name = "problem_set_description")
    val description: String,

    @OneToMany
    val problems: List<Problem> = mutableListOf(),
)
