package io.csbroker.apiserver.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "major")
class Major(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "major_id")
    val id: Long = 0,

    @Column(name = "major_name", columnDefinition = "VARCHAR(100)")
    val name: String,
) : BaseEntity()
