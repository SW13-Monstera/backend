package io.csbroker.apiserver.common.enums

enum class ErrorCode(
    val code: Int,
    val message: String,
) {
    CONDITION_NOT_FULFILLED(400, "올바르지 않은 요청입니다."),
    TOKEN_NOT_EXPIRED(400, "만료되지 않은 토큰입니다."),
    ACCESS_TOKEN_NOT_EXIST(401, "액세스 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_EXIST(401, "리프레시 토큰이 존재하지 않습니다."),
    TOKEN_MISS_MATCH(401, "저장된 토큰과 일치하지 않습니다."),
    TOKEN_INVALID(401, "올바르지 않은 토큰입니다."),
    UNAUTHORIZED(401, "인증되지 않은 사용자입니다."),
    INVALID_REDIRECT_URI(401, "올바르지 않은 redirect uri입니다."),
    PASSWORD_MISS_MATCH(401, "비밀번호가 올바르지 않습니다."),
    FORBIDDEN(403, "이 작업에 대한 권한이 없습니다."),
    NOT_FOUND_ENTITY(404, "대상을 찾을 수 없습니다."),
    USERNAME_DUPLICATED(409, "닉네임이 중복되었습니다."),
    TAG_DUPLICATED(409, "태그가 중복되었습니다."),
    EMAIL_DUPLICATED(409, "이메일이 중복되었습니다."),
    PROVIDER_MISS_MATCH(409, "올바르지 않은 provider입니다."),
    SERVER_ERROR(500, "서버에서 오류가 발생했습니다."),
}
