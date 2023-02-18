package io.csbroker.apiserver.common.filter

import io.csbroker.apiserver.common.util.log
import org.slf4j.MDC
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.InputStream
import java.util.UUID
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class LoggingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        MDC.put("traceId", UUID.randomUUID().toString())
        if (
            isAsyncDispatch(request) ||
            request.requestURI.contains("upload") ||
            request.requestURI.contains("actuator")
        ) {
            filterChain.doFilter(request, response)
        } else {
            doFilterWrapped(
                RequestWrapper(request),
                ResponseWrapper(response),
                filterChain
            )
        }
        MDC.clear()
    }

    protected fun doFilterWrapped(
        requestWrapper: RequestWrapper,
        responseWrapper: ResponseWrapper,
        filterChain: FilterChain
    ) {
        try {
            logRequest(requestWrapper)
            filterChain.doFilter(requestWrapper, responseWrapper)
        } finally {
            logResponse(responseWrapper)
            responseWrapper.copyBodyToResponse()
        }
    }

    private fun logRequest(requestWrapper: RequestWrapper) {
        val queryString = requestWrapper.queryString

        log.info(
            "Request : {} uri[{}] content-type=[{}]",
            requestWrapper.method,
            if (queryString == null) requestWrapper.requestURI else requestWrapper.requestURI + queryString,
            requestWrapper.contentType
        )

        logPayload("Request", requestWrapper.contentType, requestWrapper.inputStream)
    }

    private fun logResponse(responseWrapper: ResponseWrapper) {
        logPayload("Response", responseWrapper.contentType, responseWrapper.contentInputStream)
    }

    private fun logPayload(prefix: String, contentType: String?, inputStream: InputStream) {
        val content = StreamUtils.copyToByteArray(inputStream).contentToString()
        val mediaType = contentType?.let { MediaType.valueOf(it) } ?: MediaType.APPLICATION_JSON
        val payload = getPayload(mediaType, content)
        log.info("$prefix Payload: $payload")
    }

    private fun getPayload(mediaType: MediaType, content: String) = if (isVisible(mediaType)) {
        content.replace("\"originalPassword\":\".*\"".toRegex(), "\"originalPassword\":\"*****\"")
            .replace("\"password\":\".*\"".toRegex(), "\"password\":\"*****\"")
    } else {
        "Binary Contents"
    }

    private fun isVisible(mediaType: MediaType): Boolean {
        return VISIBLE_TYPES.stream().anyMatch {
            it.includes(mediaType)
        }
    }

    companion object {
        private val VISIBLE_TYPES: List<MediaType> = listOf(
            MediaType.valueOf("text/*"),
            MediaType.APPLICATION_FORM_URLENCODED,
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.valueOf("application/*+json"),
            MediaType.valueOf("application/*+xml"),
            MediaType.MULTIPART_FORM_DATA
        )
    }
}
