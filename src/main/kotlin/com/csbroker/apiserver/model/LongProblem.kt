package com.csbroker.apiserver.model

import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "long_problem")
@DiscriminatorValue("long")
class LongProblem(
    title: String,
    description: String,
    creator: User,

    @Column(name = "is_gradable")
    var isGradable: Boolean,

    @Column(name = "standard_answer", columnDefinition = "VARCHAR(300)")
    var standardAnswer: String
) : Problem(title = title, description = description, creator = creator)
