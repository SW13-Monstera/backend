package io.csbroker.apiserver.common.enums

enum class Role(
    val code: String,
    val displayName: String
) {
    ROLE_ADMIN("ROLE_ADMIN", "관리자 권한"),
    ROLE_USER("ROLE_USER", "일반 사용자 권한"),
    ROLE_BUSINESS("ROLE_BUSINESS", "비즈니스 사용자 권한");

    companion object {
        fun of(code: String): Role {
            val role = Role.values()
                .find { role -> role.code == code }

            role?.let {
                return it
            }.let {
                return Role.ROLE_USER
            }
        }
    }
}
