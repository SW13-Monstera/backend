package io.csbroker.apiserver.model

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.dto.user.UserResponseDto
import io.csbroker.apiserver.dto.user.UserUpdateRequestDto
import org.hibernate.annotations.GenericGenerator
import java.util.UUID
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

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

    @Column(name = "job_objective")
    var jobObjective: String? = null,

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
    val gradingHistories: MutableList<GradingHistory> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val notifications: MutableList<Notification> = mutableListOf(),
) : BaseEntity() {
    fun updateInfo(userUpdateRequestDto: UserUpdateRequestDto) {
        profileImageUrl = userUpdateRequestDto.profileImageUrl ?: profileImageUrl
        username = userUpdateRequestDto.username ?: username
        password = userUpdateRequestDto.password ?: password
        major = userUpdateRequestDto.major ?: major
        job = userUpdateRequestDto.job ?: job
        jobObjective = userUpdateRequestDto.jobObjective ?: jobObjective
        tech = userUpdateRequestDto.techs?.joinToString() ?: tech
        githubUrl = userUpdateRequestDto.githubUrl ?: githubUrl
        linkedinUrl = userUpdateRequestDto.linkedinUrl ?: linkedinUrl
    }

    fun toUserResponseDto(): UserResponseDto {
        return UserResponseDto(
            id = id!!,
            email = email,
            username = username,
            role = role,
            job = job,
            jobObjective = jobObjective,
            techs = tech?.split(", ") ?: emptyList(),
            major = major,
            profileImgUrl = profileImageUrl,
            githubUrl = githubUrl,
            linkedinUrl = linkedinUrl,
            providerType = providerType,
        )
    }

    fun encodePassword(encodedPassword: String) {
        password = encodedPassword
    }
}
