package io.csbroker.apiserver.controller.v1.problem

import io.csbroker.apiserver.common.enums.AssessmentType
import io.csbroker.apiserver.controller.RestDocsTest
import io.csbroker.apiserver.dto.problem.ProblemPageResponseDto
import io.csbroker.apiserver.dto.problem.ProblemResponseDto
import io.csbroker.apiserver.dto.problem.grade.AssessmentRequestDto
import io.csbroker.apiserver.service.problem.CommonProblemService
import io.mockk.every
import io.mockk.mockk
import io.restassured.http.Method
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters

class ProblemControllerTest : RestDocsTest() {
    private lateinit var mockMvc: MockMvcRequestSpecification
    private lateinit var commonProblemService: CommonProblemService

    @BeforeEach
    fun setUp() {
        commonProblemService = mockk()
        mockMvc = mockMvc(
            ProblemController(
                commonProblemService,
            ),
        ).header("Authorization", "Bearer TEST-TOKEN")
    }

    @Test
    fun `problem 검색`() {
        // given
        val query = "test"
        val isSolved = false
        val tags = "os,ds"
        val page = 0
        val size = 10
        val type = "long"
        val isGradable = false
        val shuffle = true
        val seed = 42L

        every { commonProblemService.findProblems(any()) } returns ProblemPageResponseDto(
            PageImpl(
                listOf(
                    ProblemResponseDto(
                        id = 1,
                        title = "test",
                        tags = listOf("os", "ds"),
                        avgScore = 1.0,
                        totalSubmission = 10,
                        type = "long",
                    ),
                ),
                PageRequest.of(0, 10),
                1,
            ),
        )

        // when
        val result = mockMvc.request(
            Method.GET,
            "/api/v1/problems?query=$query&isSolved=$isSolved&tags=$tags" +
                "&page=$page&size=$size&type=$type&isGradable=$isGradable&shuffle=$shuffle&seed=$seed",
        )
        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "problems/search",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰 ( 푼 문제로 검색을 하는 경우가 아니라면, 포함하지 않아도 됨. )")
                            .optional(),
                    ),
                    requestParameters(
                        parameterWithName("query").description("검색어"),
                        parameterWithName("isSolved").description("풀이 여부"),
                        parameterWithName("tags").description("문제의 태그들"),
                        parameterWithName("page").description("페이지"),
                        parameterWithName("size").description("가져올 문제의 개수"),
                        parameterWithName("type").description("문제의 type ( long, short, multiple )"),
                        parameterWithName("isGradable").description("채점 가능 여부"),
                        parameterWithName("shuffle").description("문제 셔플링 여부"),
                        parameterWithName("seed").description("랜덤 시드"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.contents").type(JsonFieldType.ARRAY).description("문제 데이터"),
                        fieldWithPath("data.contents.[].id").type(JsonFieldType.NUMBER).description("문제 id"),
                        fieldWithPath("data.contents.[].title").type(JsonFieldType.STRING)
                            .description("문제 제목"),
                        fieldWithPath("data.contents.[].tags").type(JsonFieldType.ARRAY).description("태그"),
                        fieldWithPath("data.contents.[].avgScore").type(JsonFieldType.NUMBER)
                            .description("평균 점수 ( 푼 사람이 없는 경우 null return )").optional(),
                        fieldWithPath("data.contents.[].totalSubmission").type(JsonFieldType.NUMBER)
                            .description("총 제출 수"),
                        fieldWithPath("data.contents.[].type").type(JsonFieldType.STRING)
                            .description("문제의 타입 ( short, multiple, choice )"),
                        fieldWithPath("data.currentPage").type(JsonFieldType.NUMBER)
                            .description("현재 페이지 번호"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER)
                            .description("검색된 페이지 수"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER)
                            .description("검색된 전체 데이터 수"),
                        fieldWithPath("data.numberOfElements").type(JsonFieldType.NUMBER)
                            .description("전체 데이터 중 현재 페이지의 데이터 수"),
                        fieldWithPath("data.size").type(JsonFieldType.NUMBER)
                            .description("요청한 데이터 수"),
                    ),
                ),
            )
    }

    @Test
    fun `Assessment grading result`() {
        // given
        every { commonProblemService.gradingAssessment(any(), any(), any()) } returns 1L

        // when
        val result = mockMvc.body(
            AssessmentRequestDto(
                assessmentType = AssessmentType.GOOD,
                content = "test",
            ),
        ).request(
            Method.POST,
            "/api/v1/problems/grade/{grading_history_id}/assessment",
            1L,
        )

        // then
        result.then()
            .statusCode(200)
            .apply(
                document(
                    "problems/grade/assessment",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("assessmentType").type(JsonFieldType.STRING)
                            .description("평가 의견 타입 ( 좋음 : GOOD, 나쁨 : BAD, 적당 : NORMAL )"),
                        fieldWithPath("content").type(JsonFieldType.STRING)
                            .description("평가 의견 내용 ( 없어도 상관 없음, 최대 150자 )"),
                    ),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증을 위한 Access 토큰")
                            .optional(),
                    ),
                    pathParameters(
                        parameterWithName("grading_history_id").description("문제 채점 id"),
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.STRING).description("결과 상태"),
                        fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("평가 의견 아이디"),
                    ),
                ),
            )
    }
}
