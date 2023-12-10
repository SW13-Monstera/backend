package io.csbroker.apiserver.common.handler

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.BizException
import io.csbroker.apiserver.common.exception.InternalServiceException
import io.csbroker.apiserver.common.util.log
import io.csbroker.apiserver.dto.common.ApiResponse
import io.sentry.Sentry
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(value = [BizException::class])
    fun handlingBizException(bizException: BizException): ResponseEntity<ApiResponse<String>> {
        log.error(bizException.log, bizException)
        return ResponseEntity.status(bizException.errorCode.code)
            .body(ApiResponse.fail(bizException.errorCode.message))
    }

    @ExceptionHandler(
        value = [
            IllegalArgumentException::class,
            MethodArgumentTypeMismatchException::class,
            HttpRequestMethodNotSupportedException::class,
        ],
    )
    fun handlingBizException(exception: Exception): ResponseEntity<ApiResponse<String>> {
        log.error(exception.message, exception)
        return ResponseEntity.status(ErrorCode.CONDITION_NOT_FULFILLED.code)
            .body(ApiResponse.fail(ErrorCode.CONDITION_NOT_FULFILLED.message))
    }

    @ExceptionHandler(value = [InternalServiceException::class])
    fun handlingInternalServiceException(internalServiceException: InternalServiceException):
        ResponseEntity<ApiResponse<String>> {
        Sentry.captureException(internalServiceException)
        log.error(internalServiceException.log, internalServiceException)

        return ResponseEntity.status(internalServiceException.errorCode.code)
            .body(ApiResponse.error(internalServiceException.errorCode.message))
    }

    @ExceptionHandler(value = [Exception::class])
    fun handlingException(exception: Exception): ResponseEntity<ApiResponse<String>> {
        Sentry.captureException(exception)
        log.error(exception.message, exception)

        if (exception is AccessDeniedException) {
            return ResponseEntity.status(ErrorCode.FORBIDDEN.code)
                .body(ApiResponse.fail(ErrorCode.FORBIDDEN.message))
        }

        return ResponseEntity.status(ErrorCode.SERVER_ERROR.code)
            .body(ApiResponse.error(ErrorCode.SERVER_ERROR.message))
    }
}
