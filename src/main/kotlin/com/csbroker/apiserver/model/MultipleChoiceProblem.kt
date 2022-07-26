package com.csbroker.apiserver.model

import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "multiple_choice_problem")
@DiscriminatorValue("multiple")
class MultipleChoiceProblem(
    title: String,
    description: String,
    creator: User,
    @Column(name = "is_multiple")
    var isMultiple: Boolean
) : Problem(title = title, description = description, creator = creator)
