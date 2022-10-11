package io.csbroker.apiserver.common.exception

import io.csbroker.apiserver.common.enums.ErrorCode

open class InternalServiceException(val errorCode: ErrorCode, val log: String) : RuntimeException()
