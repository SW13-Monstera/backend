package com.csbroker.apiserver.model

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "tag")
class Tag(
    @Id
    @GeneratedValue
    @Column(name = "tag_id")
    val id: UUID? = null,

    @Column(name = "tag_name", columnDefinition = "VARCHAR(30)")
    val name: String,

    @OneToMany(mappedBy = "tag")
    val problemTags: MutableList<ProblemTag> = mutableListOf()
)
