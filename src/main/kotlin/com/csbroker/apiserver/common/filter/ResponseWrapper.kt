package com.csbroker.apiserver.common.filter

import org.springframework.web.util.ContentCachingResponseWrapper
import javax.servlet.http.HttpServletResponse

class ResponseWrapper(response: HttpServletResponse) : ContentCachingResponseWrapper(response)
