package com.csbroker.apiserver.model

import com.csbroker.apiserver.common.enums.Role
import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue
    @Column(name = "user_id")
    val id: UUID? = null,

    @Column(name = "email", unique = true, columnDefinition = "VARCHAR(30)")
    var email: String,

    @Column(name = "username", unique = true, columnDefinition = "VARCHAR(100)")
    var username: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    var role: Role = Role.ROLE_USER,

    @Column(name = "profile_image", columnDefinition = "TEXT")
    var profileImageUrl: String? = null,

    @OneToMany(mappedBy = "creator")
    val problems: MutableList<Problem> = mutableListOf()
)
