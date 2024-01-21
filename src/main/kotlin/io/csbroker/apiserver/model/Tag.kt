package io.csbroker.apiserver.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "tag")
class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    val id: Long = 0,

    @Column(name = "tag_name", columnDefinition = "VARCHAR(30)", unique = true)
    val name: String,

    @OneToMany(mappedBy = "tag")
    val problemTags: MutableList<ProblemTag> = mutableListOf(),
) : BaseEntity()
