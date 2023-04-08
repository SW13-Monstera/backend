package io.csbroker.apiserver.e2e

import io.csbroker.apiserver.model.Tech
import io.csbroker.apiserver.repository.common.TechRepository
import io.csbroker.apiserver.repository.common.RedisRepository
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.UUID

@SpringBootTest
@AutoConfigureRestDocs
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommonControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var techRepository: TechRepository

    @Autowired
    private lateinit var redisRepository: RedisRepository

    @Test
    fun `Get stats`() {
        // given
        val statsEndPoint = "/api/v1/stats"

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(statsEndPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
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
        val statsEndPoint = "/api/v1/techs?query=a"
        techRepository.saveAll(listOf("aws", "aws s3", "java", "spring").map { Tech(name = it) })

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(statsEndPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
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
        redisRepository.setRank(
            mapOf(
                "${UUID.randomUUID()}@test1" to 100.0,
                "${UUID.randomUUID()}@test2" to 99.0,
                "${UUID.randomUUID()}@test3" to 99.0,
            ),
        )

        val ranksEndPoint = "/api/v1/ranks?size=3&page=0"

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(ranksEndPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("success")))
            .andDo(
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
}
