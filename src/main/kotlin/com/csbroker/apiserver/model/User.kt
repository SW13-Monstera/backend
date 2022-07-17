package com.csbroker.apiserver.model

import com.csbroker.apiserver.common.auth.ProviderType
import com.csbroker.apiserver.common.enums.Role
import org.hibernate.annotations.GenericGenerator
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    var id: UUID? = null,

    @Column(name = "email", unique = true, columnDefinition = "VARCHAR(30)")
    var email: String,

    @Column(name = "username", unique = true, columnDefinition = "VARCHAR(100)")
    var username: String,

    @Column(name = "password", columnDefinition = "VARCHAR(50)")
    var password: String = "NO_PASSWORD",

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    var role: Role = Role.ROLE_USER,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type")
    var providerType: ProviderType,

    @Column(name = "profile_image", columnDefinition = "TEXT")
    var profileImageUrl: String? = null,

    @OneToMany(mappedBy = "creator")
    val problems: MutableList<Problem> = mutableListOf()
) : BaseEntity()
