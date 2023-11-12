package io.csbroker.apiserver.common.filter

import org.springframework.web.util.ContentCachingResponseWrapper
import jakarta.servlet.http.HttpServletResponse

class ResponseWrapper(response: HttpServletResponse) : ContentCachingResponseWrapper(response)
