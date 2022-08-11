package com.csbroker.apiserver.common.exception

import com.csbroker.apiserver.common.enums.ErrorCode

class UnAuthorizedException(
    errorCode: ErrorCode,
    log: String
) : BizException(errorCode, log)
