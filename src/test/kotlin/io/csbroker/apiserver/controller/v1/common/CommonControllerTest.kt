package io.csbroker.apiserver.controller.v1.common

import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.StatsDto
import io.csbroker.apiserver.dto.common.ChatCompletionRequestDto
import io.csbroker.apiserver.dto.common.RankListDto
import io.csbroker.apiserver.service.common.ChatService
import io.csbroker.apiserver.service.common.CommonService
import io.csbroker.apiserver.service.common.S3Service
import io.csbroker.apiserver.service.user.UserService
import io.mockk.every
import io.mockk.mockk
import io.restassured.http.Method
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import java.util.UUID

class CommonControllerTest : RestDocsTest() {
    private lateinit var mockMvc: MockMvcRequestSpecification
    private lateinit var commonService: CommonService
    private lateinit var s3Service: S3Service
    private lateinit var userService: UserService
    private lateinit var chatService: ChatService

    @BeforeEach
    fun setUp() {
        commonService = mockk()
        s3Service = mockk()
        userService = mockk()
        chatService = mockk()
        mockMvc = mockMvc(
            CommonController(
                commonService,
                s3Service,
                userService,
                chatService,
            ),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `Get stats`() {
        // given
        every { commonService.getStats() } returns StatsDto(
            10,
            10,
            100,
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/v1/stats")

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "common/stats",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data.problemCnt")
                            .type(JsonFieldType.NUMBER).description("문제 수"),
                        PayloadDocumentation.fieldWithPath("data.gradableProblemCnt")
                            .type(JsonFieldType.NUMBER).description("채점 가능한 문제 수"),
                        PayloadDocumentation.fieldWithPath("data.userCnt")
                            .type(JsonFieldType.NUMBER).description("회원 수"),
                    ),
                ),
            )
    }

    @Test
    fun `Get techs`() {
        // given
        every { commonService.findTechByQuery(any()) } returns listOf("aws", "android")

        // when
        val result = mockMvc.request(Method.GET, "/api/v1/techs?query=a")

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "common/techs",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    requestParameters(
                        parameterWithName("query").description("검색어 ( 필수 )"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data")
                            .type(JsonFieldType.ARRAY).description("기술 데이터"),
                    ),
                ),
            )
    }

    @Test
    fun `Get ranks`() {
        // given
        every { commonService.getRanks(any(), any()) } returns RankListDto(
            3,
            10,
            0,
            1,
            listOf(
                RankListDto.RankDetail(
                    UUID.randomUUID(),
                    "test",
                    1,
                    10.0,
                ),
            ),
        )

        // when
        val result = mockMvc.request(Method.GET, "/api/v1/ranks?size=3&page=0")

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "common/ranks",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    requestParameters(
                        parameterWithName("size").description("가져올 첫 랭킹 ( 1 이상 )"),
                        parameterWithName("page").description("가져올 페이지 ( 0 이상 )"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data")
                            .type(JsonFieldType.OBJECT).description("랭킹 데이터"),
                        PayloadDocumentation.fieldWithPath("data.size")
                            .type(JsonFieldType.NUMBER).description("가져온 랭킹 데이터 사이즈"),
                        PayloadDocumentation.fieldWithPath("data.totalPage")
                            .type(JsonFieldType.NUMBER).description("총 페이지 수"),
                        PayloadDocumentation.fieldWithPath("data.currentPage")
                            .type(JsonFieldType.NUMBER).description("현재 페이지 수"),
                        PayloadDocumentation.fieldWithPath("data.numberOfElements")
                            .type(JsonFieldType.NUMBER).description("총 데이터 수"),
                        PayloadDocumentation.fieldWithPath("data.contents")
                            .type(JsonFieldType.ARRAY).description("랭킹 데이터"),
                        PayloadDocumentation.fieldWithPath("data.contents.[].id")
                            .type(JsonFieldType.STRING).description("유저 id"),
                        PayloadDocumentation.fieldWithPath("data.contents.[].username")
                            .type(JsonFieldType.STRING).description("유저 닉네임"),
                        PayloadDocumentation.fieldWithPath("data.contents.[].rank")
                            .type(JsonFieldType.NUMBER).description("유저 랭킹"),
                        PayloadDocumentation.fieldWithPath("data.contents.[].score")
                            .type(JsonFieldType.NUMBER).description("유저 점수"),
                    ),
                ),
            )
    }

    @Test
    fun `Get majors`() {
        // given
        every { commonService.findMajorByQuery(any()) } returns listOf("컴퓨터공학과", "컴퓨터교육과")

        // when
        val result = mockMvc.request(Method.GET, "/api/v1/majors?query=컴퓨터")

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "common/majors",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    requestParameters(
                        parameterWithName("query").description("검색어 ( 필수 )"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data")
                            .type(JsonFieldType.ARRAY).description("전공 데이터"),
                    ),
                ),
            )
    }

    @Test
    fun `Get answer by AI`() {
        // given
        every { chatService.completeChat(any(), any()) } returns "정답!"

        // when
        val result = mockMvc.body(ChatCompletionRequestDto("민재원은 잘생겼나요?")).request(Method.POST, "/api/v1/chat")

        // then
        result.then()
            .statusCode(200)
            .apply(
                MockMvcRestDocumentation.document(
                    "common/majors",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    requestFields(
                        PayloadDocumentation.fieldWithPath("content").type(JsonFieldType.STRING)
                            .description("답변을 받고 싶은 질문"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("status")
                            .type(JsonFieldType.STRING).description("결과 상태"),
                        PayloadDocumentation.fieldWithPath("data")
                            .type(JsonFieldType.STRING).description("답변 데이터"),
                    ),
                ),
            )
    }
}
