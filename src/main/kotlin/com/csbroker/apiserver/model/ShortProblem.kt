package com.csbroker.apiserver.model

import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "short_problem")
@DiscriminatorValue("short")
class ShortProblem(
    title: String,
    description: String,
    creator: User,

    @Column(name = "answer")
    val answer: String
) : Problem(title = title, description = description, creator = creator)
