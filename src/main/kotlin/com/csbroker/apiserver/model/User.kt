package com.csbroker.apiserver.model

import com.csbroker.apiserver.auth.ProviderType
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.dto.user.UserResponseDto
import com.csbroker.apiserver.dto.user.UserUpdateRequestDto
import org.hibernate.annotations.GenericGenerator
import java.util.UUID
import javax.persistence.CascadeType
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

    @Column(name = "provider_id")
    var providerId: String? = null,

    @Column(name = "profile_image", columnDefinition = "TEXT")
    var profileImageUrl: String? = null,

    @Column(name = "major")
    var major: String? = null,

    @Column(name = "job")
    var job: String? = null,

    @Column(name = "tech")
    var tech: String? = null,

    @Column(name = "github_url")
    var githubUrl: String? = null,

    @Column(name = "linkedin_url")
    var linkedinUrl: String? = null,

    @Column(name = "is_deleted", columnDefinition = "boolean default 0")
    var isDeleted: Boolean = false,

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    val problems: MutableList<Problem> = mutableListOf(),

    @OneToMany(mappedBy = "assignedUser", fetch = FetchType.LAZY)
    val assignedAnswers: MutableList<UserAnswer> = mutableListOf(),

    @OneToMany(mappedBy = "validatingUser", fetch = FetchType.LAZY)
    val assignedToValidateAnswers: MutableList<UserAnswer> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val gradingHistories: MutableList<GradingHistory> = mutableListOf()
) : BaseEntity() {
    fun updateInfo(userUpdateRequestDto: UserUpdateRequestDto) {
        this.profileImageUrl = userUpdateRequestDto.profileImageUrl ?: this.profileImageUrl
        this.username = userUpdateRequestDto.username ?: this.username
        this.password = userUpdateRequestDto.password ?: this.password
        this.major = userUpdateRequestDto.major ?: this.major
        this.job = userUpdateRequestDto.job ?: this.job
        this.tech = userUpdateRequestDto.techs?.joinToString() ?: this.tech
        this.githubUrl = userUpdateRequestDto.githubUrl ?: this.githubUrl
        this.linkedinUrl = userUpdateRequestDto.linkedinUrl ?: this.linkedinUrl
    }

    fun toUserResponseDto(): UserResponseDto {
        return UserResponseDto(
            id = this.id!!,
            email = this.email,
            username = this.username,
            role = this.role,
            job = this.job,
            techs = this.tech?.split(", ") ?: emptyList(),
            major = this.major,
            githubUrl = this.githubUrl,
            linkedinUrl = this.linkedinUrl
        )
    }

    fun encodePassword(encodedPassword: String) {
        this.password = encodedPassword
    }
}
