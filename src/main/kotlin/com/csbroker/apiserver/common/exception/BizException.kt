package com.csbroker.apiserver.common.exception

import com.csbroker.apiserver.common.enums.ErrorCode

open class BizException(val errorCode: ErrorCode, val log: String) : RuntimeException()
