package io.csbroker.apiserver.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "tech")
class Tech(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tech_id")
    val id: Long = 0,

    @Column(name = "tech_name", columnDefinition = "VARCHAR(100)")
    val name: String,
) : BaseEntity()
