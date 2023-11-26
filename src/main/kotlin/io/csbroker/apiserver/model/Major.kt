package io.csbroker.apiserver.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

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
