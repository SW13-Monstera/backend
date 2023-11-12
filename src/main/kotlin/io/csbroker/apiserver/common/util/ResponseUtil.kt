package io.csbroker.apiserver.common.util

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import jakarta.servlet.http.HttpServletResponse

fun HttpServletResponse.setStatus(status: HttpStatus) {
    this.status = status.value()
}

fun HttpServletResponse.setJsonResponseBody(responseBody: ByteArray) {
    this.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    this.setContentLength(responseBody.size)
    this.outputStream.write(responseBody)
}
