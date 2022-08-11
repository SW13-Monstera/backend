package com.csbroker.apiserver.common.exception

import com.csbroker.apiserver.common.enums.ErrorCode

class ConditionConflictException(
    errorCode: ErrorCode,
    log: String
) : BizException(errorCode, log)
