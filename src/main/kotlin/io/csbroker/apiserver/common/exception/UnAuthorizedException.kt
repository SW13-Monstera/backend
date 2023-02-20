package io.csbroker.apiserver.common.exception

import io.csbroker.apiserver.common.enums.ErrorCode

class UnAuthorizedException(
    errorCode: ErrorCode,
    log: String,
) : BizException(errorCode, log)
