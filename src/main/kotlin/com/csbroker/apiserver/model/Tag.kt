package com.csbroker.apiserver.model

import org.hibernate.annotations.GenericGenerator
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
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "tag_id", columnDefinition = "BINARY(16)")
    val id: UUID? = null,

    @Column(name = "tag_name", columnDefinition = "VARCHAR(30)")
    val name: String,

    @OneToMany(mappedBy = "tag")
    val problemTags: MutableList<ProblemTag> = mutableListOf()
) : BaseEntity()
