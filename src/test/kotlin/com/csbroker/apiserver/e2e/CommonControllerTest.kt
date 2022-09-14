package com.csbroker.apiserver.e2e

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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureRestDocs
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommonControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `Get stats`() {
        // given
        val statsEndPoint = "/api/v1/stats"

        // when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get(statsEndPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
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
                            .type(JsonFieldType.NUMBER).description("회원 수")
                    )
                )
            )
    }
}
