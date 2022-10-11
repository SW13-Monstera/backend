package io.csbroker.apiserver.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "tag")
class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    val id: Long? = null,

    @Column(name = "tag_name", columnDefinition = "VARCHAR(30)")
    val name: String,

    @OneToMany(mappedBy = "tag")
    val problemTags: MutableList<ProblemTag> = mutableListOf()
) : BaseEntity()
