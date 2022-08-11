package com.csbroker.apiserver.common.exception

import com.csbroker.apiserver.common.enums.ErrorCode

class EntityNotFoundException(log: String) : BizException(ErrorCode.NOT_FOUND_ENTITY, log)
