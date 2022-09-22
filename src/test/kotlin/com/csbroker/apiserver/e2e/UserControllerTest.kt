package com.csbroker.apiserver.e2e

import com.csbroker.apiserver.auth.AuthTokenProvider
import com.csbroker.apiserver.auth.ProviderType
import com.csbroker.apiserver.common.enums.GradingStandardType
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.dto.user.UserUpdateRequestDto
import com.csbroker.apiserver.model.GradingHistory
import com.csbroker.apiserver.model.GradingStandard
import com.csbroker.apiserver.model.LongProblem
import com.csbroker.apiserver.model.ProblemTag
import com.csbroker.apiserver.model.Tag
import com.csbroker.apiserver.model.User
import com.csbroker.apiserver.repository.GradingHistoryRepository
import com.csbroker.apiserver.repository.ProblemRepository
import com.csbroker.apiserver.repository.ProblemTagRepository
import com.csbroker.apiserver.repository.TagRepository
import com.csbroker.apiserver.repository.UserRepository
import com.csbroker.apiserver.repository.common.RedisRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Date
import java.util.UUID

@SpringBootTest
@AutoConfigureRestDocs
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var tokenProvider: AuthTokenProvider

    @Autowired
    private lateinit var problemRepository: ProblemRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var tagRepository: TagRepository

    @Autowired
    private lateinit var problemTagRepository: ProblemTagRepository

    @Autowired
    private lateinit var gradingHistoryRepository: GradingHistoryRepository

    @Autowired
    private lateinit var redisRepository: RedisRepository

    private val USER_ENDPOINT = "/api/v1/users"

    private lateinit var adminId: UUID

    @BeforeAll
    fun setUpData() {
        for (i in 1..10) {
            val user = User(
                email = "test-user$i@test.com",
                username = "test-user$i",
                providerType = ProviderType.LOCAL
            )
            userRepository.save(user)
        }

        val admin = User(
            email = "test-admin@test.com",
            username = "test-admin",
            role = Role.ROLE_ADMIN,
            providerType = ProviderType.LOCAL,
            major = "컴퓨터공학",
            job = "백엔드 개발자",
            tech = "Spring, Docker, Kotlin",
            githubUrl = "https://github.com/kshired",
            linkedinUrl = "https://www.linkedin.com/in/seongil-kim-40773b23b/"
        )

        userRepository.save(admin)

        adminId = admin.id!!

        val osTag = Tag(
            name = "os"
        )
        tagRepository.save(osTag)

        val dsTag = Tag(
            name = "ds"
        )
        tagRepository.save(dsTag)

        val networkTag = Tag(
            name = "network"
        )
        tagRepository.save(networkTag)

        val dbTag = Tag(
            name = "db"
        )
        tagRepository.save(dbTag)

        for (i in 0..10) {
            val problem = LongProblem(
                title = "test$i",
                description = "test",
                creator = admin,
                standardAnswer = "test"
            )

            val gradingStandard = GradingStandard(
                content = "test",
                score = 10.0,
                type = GradingStandardType.KEYWORD,
                problem = problem
            )

            problem.addGradingStandards(listOf(gradingStandard))
            problemRepository.save(problem)

            val gradingHistory = GradingHistory(
                problem = problem,
                user = admin,
                userAnswer = "test",
                score = i.toDouble()
            )

            gradingHistoryRepository.save(gradingHistory)

            if (i == 10) {
                val problemTagOS = ProblemTag(
                    problem = problem,
                    tag = osTag
                )

                val problemTagDB = ProblemTag(
                    problem = problem,
                    tag = dbTag
                )

                val problemTagDs = ProblemTag(
                    problem = problem,
                    tag = dsTag
                )

                val problemTagNetwork = ProblemTag(
                    problem = problem,
                    tag = networkTag
                )

                problemTagRepository.save(problemTagOS)
                problemTagRepository.save(problemTagDB)
                problemTagRepository.save(problemTagDs)
                problemTagRepository.save(problemTagNetwork)
            }
        }
    }

    @Test
    @Order(1)
    fun `GetUser v1 200 OK`() {
        // given
        val now = Date()
        val urlTemplate = "$USER_ENDPOINT/{user_id}"

        val accessToken = tokenProvider.createAuthToken(
            "test-admin@test.com",
            expiry = Date(now.time + 6000000),
            role = Role.ROLE_ADMIN.code
        )

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(urlTemplate, "$adminId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(content().string(containsString("success")))
            .andDo(
                document(
                    "users/findOne",
                    preprocessResponse(Preprocessors.prettyPrint()),
                    pathParameters(
                        parameterWithName("user_id").description("회원 UUID")
                    ),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Access 토큰 ( JWT )")
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
                            .type(JsonFieldType.STRING).description("LinkedIn url").optional()
                    )
                )
            )
    }

    @Test
    @Order(2)
    fun `GetUsers v1 200 OK`() {
        // given
        val now = Date()
        val accessToken = tokenProvider.createAuthToken(
            "test-admin@test.com",
            expiry = Date(now.time + 6000000),
            role = Role.ROLE_ADMIN.code
        )

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get(USER_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(content().string(containsString("success")))
            .andDo(
                document(
                    "users/findAll",
                    preprocessResponse(Preprocessors.prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Admin 권한의 Access 토큰 ( JWT )")
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
                            .type(JsonFieldType.STRING).description("LinkedIn url").optional()
                    )
                )
            )
    }

    @Test
    @Order(3)
    fun `UpdateUser v1 200 OK`() {
        // given
        val now = Date()
        val urlTemplate = "$USER_ENDPOINT/$adminId"
        val accessToken = tokenProvider.createAuthToken(
            "test-admin@test.com",
            expiry = Date(now.time + 6000000),
            role = Role.ROLE_ADMIN.code
        )
        val userUpdateRequestDto = UserUpdateRequestDto(
            username = "test-admin-update",
            profileImageUrl = "https://test.com/test.png",
            password = "changePassword123!",
            major = "환경공학",
            job = "대학생",
            jobObjective = "프론트엔드 개발자",
            techs = listOf("react", "typescript"),
            githubUrl = "https://github.com/Kim-Hyunjo",
            linkedinUrl = "https://www.linkedin.com/in/%EC%9E%AC%EC%9B%90-%EB%AF%BC-2b5149211"
        )

        val userUpdateRequestDtoString = objectMapper.writeValueAsString(userUpdateRequestDto)

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.put(urlTemplate)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userUpdateRequestDtoString)
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(content().string(containsString("success")))
            .andDo(
                document(
                    "users/update",
                    preprocessRequest(Preprocessors.prettyPrint()),
                    preprocessResponse(Preprocessors.prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Access 토큰 ( JWT )")
                    ),
                    PayloadDocumentation.requestFields(
                        fieldWithPath("username").type(JsonFieldType.STRING)
                            .description("수정할 닉네임 ( 필수 X )").optional(),
                        fieldWithPath("profileImageUrl").type(JsonFieldType.STRING)
                            .description("수정할 프로필 이미지 url ( 필수 X )").optional(),
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
                            .description("수정할 linkedin url ( 필수 X )").optional()
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
                            .type(JsonFieldType.STRING).description("LinkedIn url").optional()
                    )
                )
            )
    }

    @Test
    @Order(4)
    fun `Delete User v1 200 OK`() {
        // given
        val now = Date()
        val urlTemplate = "$USER_ENDPOINT/{user_id}"

        val accessToken = tokenProvider.createAuthToken(
            "test-admin@test.com",
            expiry = Date(now.time + 6000000),
            role = Role.ROLE_ADMIN.code
        )

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.delete(urlTemplate, "$adminId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(content().string(containsString("success")))
            .andDo(
                document(
                    "users/deleteOne",
                    preprocessResponse(Preprocessors.prettyPrint()),
                    pathParameters(
                        parameterWithName("user_id").description("회원 UUID")
                    ),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Access 토큰 ( JWT )")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("UUID"),
                        fieldWithPath("data.result").type(JsonFieldType.BOOLEAN).description("삭제 결과")
                    )
                )
            )
    }

    @Test
    @Order(5)
    fun `Get User Stat v1 200 OK`() {
        // given
        val now = Date()
        val urlTemplate = "$USER_ENDPOINT/{user_id}/stats"

        val accessToken = tokenProvider.createAuthToken(
            "test-admin@test.com",
            expiry = Date(now.time + 6000000),
            role = Role.ROLE_ADMIN.code
        )

        redisRepository.setRank(mapOf(adminId to 100.0))

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(urlTemplate, "$adminId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.token}")
                .accept(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(status().isOk)
            .andExpect(content().string(containsString("success")))
            .andDo(
                document(
                    "users/statsOne",
                    preprocessResponse(Preprocessors.prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("Admin 권한의 Access 토큰 ( JWT )")
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
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER).description("점수")
                    )
                )
            )
    }
}
