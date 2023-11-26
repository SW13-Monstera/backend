package io.csbroker.apiserver.controller.v1.user

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.user.UserStatsDto
import io.csbroker.apiserver.dto.user.UserUpdateRequestDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.service.user.UserService
import io.mockk.every
import io.mockk.mockk
import io.restassured.http.Method
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import java.util.UUID

class UserControllerTest : RestDocsTest() {
    private val USER_ENDPOINT = "/api/v1/users"
    private lateinit var mockMvc: MockMvcRequestSpecification
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userService = mockk()
        mockMvc = mockMvc(
            UserController(userService),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `GetUser v1 200 OK`() {
        // given
        every { userService.findUserById(any()) } returns User(
            id = UUID.randomUUID(),
            email = "email",
            username = "username",
            providerType = ProviderType.LOCAL,
            major = "major",
            job = "job",
            jobObjective = "jobObjective",
            tech = "tech",
            profileImageUrl = "profileImageUrl",
            githubUrl = "githubUrl",
            linkedinUrl = "linkedinUrl",
        )

        // when
        val result = mockMvc.request(Method.GET, "$USER_ENDPOINT/{user_id}", UUID.randomUUID().toString())

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "users/findOne",
                    preprocessResponse(Preprocessors.prettyPrint()),
                    pathParameters(
                        parameterWithName("user_id").description("회원 UUID"),
                    ),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Access 토큰 ( JWT )"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("UUID"),
                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("data.username").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("data.role").type(JsonFieldType.STRING).description("권한"),
                        fieldWithPath("data.major")
                            .type(JsonFieldType.STRING).description("전공").optional(),
                        fieldWithPath("data.job")
                            .type(JsonFieldType.STRING).description("직업").optional(),
                        fieldWithPath("data.jobObjective")
                            .type(JsonFieldType.STRING).description("희망 직무").optional(),
                        fieldWithPath("data.techs")
                            .type(JsonFieldType.ARRAY).description("사용 기술").optional(),
                        fieldWithPath("data.profileImgUrl")
                            .type(JsonFieldType.STRING).description("프로필 이미지 url").optional(),
                        fieldWithPath("data.githubUrl")
                            .type(JsonFieldType.STRING).description("Github url").optional(),
                        fieldWithPath("data.linkedinUrl")
                            .type(JsonFieldType.STRING).description("LinkedIn url").optional(),
                        fieldWithPath("data.providerType")
                            .type(JsonFieldType.STRING).description("Provider Type ( GOOGLE, GITHUB, LOCAL )"),
                    ),
                ),
            )
    }

    @Test
    fun `GetUsers v1 200 OK`() {
        // given
        every { userService.findUsers() } returns listOf(
            User(
                id = UUID.randomUUID(),
                email = "email",
                username = "username",
                providerType = ProviderType.LOCAL,
                major = "major",
                job = "job",
                jobObjective = "jobObjective",
                tech = "tech",
                profileImageUrl = "profileImageUrl",
                githubUrl = "githubUrl",
                linkedinUrl = "linkedinUrl",
            ),
        )

        // when
        val result = mockMvc.get(USER_ENDPOINT)

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "users/findAll",
                    preprocessResponse(Preprocessors.prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Admin 권한의 Access 토큰 ( JWT )"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.[].id").type(JsonFieldType.STRING).description("UUID"),
                        fieldWithPath("data.[].email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("data.[].username").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("data.[].role").type(JsonFieldType.STRING).description("권한"),
                        fieldWithPath("data.[].major")
                            .type(JsonFieldType.STRING).description("전공").optional(),
                        fieldWithPath("data.[].job")
                            .type(JsonFieldType.STRING).description("직업").optional(),
                        fieldWithPath("data.[].jobObjective")
                            .type(JsonFieldType.STRING).description("희망 직무").optional(),
                        fieldWithPath("data.[].techs")
                            .type(JsonFieldType.ARRAY).description("사용 기술").optional(),
                        fieldWithPath("data.[].profileImgUrl")
                            .type(JsonFieldType.STRING).description("프로필 이미지 url").optional(),
                        fieldWithPath("data.[].githubUrl")
                            .type(JsonFieldType.STRING).description("Github url").optional(),
                        fieldWithPath("data.[].linkedinUrl")
                            .type(JsonFieldType.STRING).description("LinkedIn url").optional(),
                        fieldWithPath("data.[].providerType")
                            .type(JsonFieldType.STRING).description("Provider Type ( GOOGLE, GITHUB, LOCAL )"),
                    ),
                ),
            )
    }

    @Test
    fun `UpdateUser v1 200 OK`() {
        // given
        every { userService.modifyUser(any(), any(), any()) } returns User(
            id = UUID.randomUUID(),
            email = "email",
            username = "username",
            providerType = ProviderType.LOCAL,
            major = "major",
            job = "job",
            jobObjective = "jobObjective",
            tech = "tech",
            profileImageUrl = "profileImageUrl",
            githubUrl = "githubUrl",
            linkedinUrl = "linkedinUrl",
        )

        // when
        val result = mockMvc.body(
            UserUpdateRequestDto(
                username = "test-admin-update",
                profileImageUrl = "https://test.com/test.png",
                password = "changePassword123!",
                major = "환경공학",
                job = "대학생",
                jobObjective = "프론트엔드 개발자",
                techs = listOf("react", "typescript"),
                githubUrl = "https://github.com/Kim-Hyunjo",
                linkedinUrl = "https://www.linkedin.com/in/%EC%9E%AC%EC%9B%90-%EB%AF%BC-2b5149211",
                originalPassword = "password",
            ),
        ).request(Method.PUT, "$USER_ENDPOINT/${UUID.randomUUID()}")

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "users/update",
                    preprocessRequest(Preprocessors.prettyPrint()),
                    preprocessResponse(Preprocessors.prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Access 토큰 ( JWT )"),
                    ),
                    PayloadDocumentation.requestFields(
                        fieldWithPath("username").type(JsonFieldType.STRING)
                            .description("수정할 닉네임 ( 필수 X )").optional(),
                        fieldWithPath("profileImageUrl").type(JsonFieldType.STRING)
                            .description("수정할 프로필 이미지 url ( 필수 X )").optional(),
                        fieldWithPath("originalPassword").type(JsonFieldType.STRING)
                            .description("수정전 비밀번호 ( 필수 X, but 수정시 필수 )").optional(),
                        fieldWithPath("password").type(JsonFieldType.STRING)
                            .description("수정할 비밀번호 ( 필수 X )").optional(),
                        fieldWithPath("major").type(JsonFieldType.STRING)
                            .description("수정할 전공 ( 필수 X )").optional(),
                        fieldWithPath("job").type(JsonFieldType.STRING)
                            .description("수정할 직업 ( 필수 X )").optional(),
                        fieldWithPath("jobObjective")
                            .type(JsonFieldType.STRING).description("수정할 희망 직무 ( 필수 X )").optional(),
                        fieldWithPath("techs").type(JsonFieldType.ARRAY)
                            .description("수정할 사용 기술 ( 필수 X )").optional(),
                        fieldWithPath("githubUrl").type(JsonFieldType.STRING)
                            .description("수정할 github url ( 필수 X )").optional(),
                        fieldWithPath("linkedinUrl").type(JsonFieldType.STRING)
                            .description("수정할 linkedin url ( 필수 X )").optional(),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("UUID"),
                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("data.username").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("data.role").type(JsonFieldType.STRING).description("권한"),
                        fieldWithPath("data.major")
                            .type(JsonFieldType.STRING).description("전공").optional(),
                        fieldWithPath("data.job")
                            .type(JsonFieldType.STRING).description("직업").optional(),
                        fieldWithPath("data.jobObjective")
                            .type(JsonFieldType.STRING).description("희망 직무").optional(),
                        fieldWithPath("data.techs")
                            .type(JsonFieldType.ARRAY).description("사용 기술").optional(),
                        fieldWithPath("data.profileImgUrl")
                            .type(JsonFieldType.STRING).description("프로필 이미지 url").optional(),
                        fieldWithPath("data.githubUrl")
                            .type(JsonFieldType.STRING).description("Github url").optional(),
                        fieldWithPath("data.linkedinUrl")
                            .type(JsonFieldType.STRING).description("LinkedIn url").optional(),
                        fieldWithPath("data.providerType")
                            .type(JsonFieldType.STRING).description("Provider Type ( GOOGLE, GITHUB, LOCAL )"),
                    ),
                ),
            )
    }

    @Test
    fun `Delete User v1 200 OK`() {
        // given
        every { userService.deleteUser(any(), any()) } returns true

        // when
        val result = mockMvc.delete("$USER_ENDPOINT/{user_id}", UUID.randomUUID().toString())

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "users/deleteOne",
                    preprocessResponse(Preprocessors.prettyPrint()),
                    pathParameters(
                        parameterWithName("user_id").description("회원 UUID"),
                    ),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Access 토큰 ( JWT )"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("UUID"),
                        fieldWithPath("data.result").type(JsonFieldType.BOOLEAN).description("삭제 결과"),
                    ),
                ),
            )
    }

    @Test
    fun `Get User Stat v1 200 OK`() {
        // given
        every { userService.getStats(any()) } returns UserStatsDto(
            correctAnsweredProblem = listOf(
                UserStatsDto.ProblemStatsDto(
                    id = 1,
                    type = "long",
                    title = "Long Problem",
                ),
            ),
            wrongAnsweredProblem = listOf(
                UserStatsDto.ProblemStatsDto(
                    id = 2,
                    type = "short",
                    title = "Short Problem",
                ),
            ),
            partialAnsweredProblem = listOf(
                UserStatsDto.ProblemStatsDto(
                    id = 3,
                    type = "short",
                    title = "Short Problem",
                ),
            ),
            mapOf("os" to 1, "network" to 2, "ds" to 3, "db" to 4),
            1,
            10.0,
        )

        // when
        val result = mockMvc.get("$USER_ENDPOINT/${UUID.randomUUID()}/stats")

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "users/statsOne",
                    preprocessResponse(Preprocessors.prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Admin 권한의 Access 토큰 ( JWT )"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.correctAnsweredProblem").type(JsonFieldType.ARRAY).description("맞은 문제"),
                        fieldWithPath("data.correctAnsweredProblem.[].id").type(JsonFieldType.NUMBER)
                            .description("문제 id"),
                        fieldWithPath("data.correctAnsweredProblem.[].type").type(JsonFieldType.STRING)
                            .description("문제 타입"),
                        fieldWithPath("data.correctAnsweredProblem.[].title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.wrongAnsweredProblem").type(JsonFieldType.ARRAY).description("틀린 문제"),
                        fieldWithPath("data.wrongAnsweredProblem.[].id").type(JsonFieldType.NUMBER)
                            .description("문제 id"),
                        fieldWithPath("data.wrongAnsweredProblem.[].type").type(JsonFieldType.STRING)
                            .description("문제 타입"),
                        fieldWithPath("data.wrongAnsweredProblem.[].title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.partialAnsweredProblem").type(JsonFieldType.ARRAY)
                            .description("부분 점수를 받은 문제"),
                        fieldWithPath("data.partialAnsweredProblem.[].id").type(JsonFieldType.NUMBER)
                            .description("문제 id"),
                        fieldWithPath("data.partialAnsweredProblem.[].type").type(JsonFieldType.STRING)
                            .description("문제 타입"),
                        fieldWithPath("data.partialAnsweredProblem.[].title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.count").type(JsonFieldType.OBJECT).description("푼 문제 수 통계"),
                        fieldWithPath("data.count.os").type(JsonFieldType.NUMBER).description("맞은 운영체제 문제 수 통계"),
                        fieldWithPath("data.count.network").type(JsonFieldType.NUMBER).description("맞은 네트워크 문제 수 통계"),
                        fieldWithPath("data.count.ds").type(JsonFieldType.NUMBER).description("맞은 자료구조 문제 수 통계"),
                        fieldWithPath("data.count.db").type(JsonFieldType.NUMBER).description("맞은 데이터베이스 문제 수 통계"),
                        fieldWithPath("data.rank").type(JsonFieldType.NUMBER).description("랭킹 ( 랭킹이 집계되지 않았다면, null )")
                            .optional(),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER).description("점수"),
                    ),
                ),
            )
    }
}
