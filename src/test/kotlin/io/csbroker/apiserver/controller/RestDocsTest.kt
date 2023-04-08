package io.csbroker.apiserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.csbroker.apiserver.auth.LoginUserArgumentResolver
import io.restassured.config.ObjectMapperConfig
import io.restassured.module.mockmvc.RestAssuredMockMvc
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.method.support.HandlerMethodArgumentResolver

@ExtendWith(RestDocumentationExtension::class)
abstract class RestDocsTest {
    private lateinit var restDocumentation: RestDocumentationContextProvider

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        this.restDocumentation = restDocumentation
    }

    protected fun mockMvc(
        controller: Any,
        controllerAdvices: Any = emptyArray<Any>(),
        argumentResolvers: Array<HandlerMethodArgumentResolver> = arrayOf(
            PageableHandlerMethodArgumentResolver(),
            LoginUserArgumentResolver(true),
        ),
        httpMessageConverters: Array<HttpMessageConverter<Any>> = arrayOf(
            MappingJackson2HttpMessageConverter(objectMapper()),
        ),
        objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule(),
    ): MockMvcRequestSpecification {
        val mockMvc = createMockMvc(
            controller,
            controllerAdvices,
            argumentResolvers,
            httpMessageConverters,
        )
        return RestAssuredMockMvc.given()
            .config(
                RestAssuredMockMvcConfig.config().objectMapperConfig(
                    ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory { _, _ -> objectMapper },
                ),
            )
            .mockMvc(mockMvc)
            .contentType("application/json")
            .accept("application/json")
    }

    private fun createMockMvc(
        controller: Any,
        controllerAdvices: Any,
        argumentResolvers: Array<HandlerMethodArgumentResolver>,
        httpMessageConverters: Array<HttpMessageConverter<Any>>,
    ): MockMvc {
        return MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(controllerAdvices)
            .setCustomArgumentResolvers(*argumentResolvers)
            .setMessageConverters(*httpMessageConverters)
            .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
            .build()
    }

    private fun objectMapper(): ObjectMapper {
        return Jackson2ObjectMapperBuilder()
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .featuresToDisable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .build()
    }
}
