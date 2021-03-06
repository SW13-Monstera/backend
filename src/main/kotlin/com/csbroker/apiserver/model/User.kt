package com.csbroker.apiserver.model

import com.csbroker.apiserver.common.auth.ProviderType
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.dto.UserResponseDto
import com.csbroker.apiserver.dto.UserUpdateRequestDto
import org.hibernate.annotations.GenericGenerator
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
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

    @Column(name = "password", columnDefinition = "VARCHAR(100)")
    var password: String = "NO_PASSWORD",

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    var role: Role = Role.ROLE_USER,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type")
    var providerType: ProviderType,

    @Column(name = "profile_image", columnDefinition = "TEXT")
    var profileImageUrl: String? = null,

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    val problems: MutableList<Problem> = mutableListOf()
) : BaseEntity() {
    fun updateInfo(userUpdateRequestDto: UserUpdateRequestDto) {
        this.profileImageUrl = userUpdateRequestDto.profileImageUrl ?: this.profileImageUrl
        this.username = userUpdateRequestDto.username ?: this.username
        this.password = userUpdateRequestDto.password ?: this.password
    }

    fun toUserResponseDto(): UserResponseDto {
        return UserResponseDto(
            id = this.id!!,
            email = this.email,
            username = this.username,
            role = this.role
        )
    }

    fun encodePassword(encodedPassword: String) {
        this.password = encodedPassword
    }
}
