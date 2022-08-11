package com.csbroker.apiserver.common.exception

import com.csbroker.apiserver.common.enums.ErrorCode

open class InternalServiceException(val errorCode: ErrorCode, val log: String) : RuntimeException()
