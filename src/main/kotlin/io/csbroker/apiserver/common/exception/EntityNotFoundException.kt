package io.csbroker.apiserver.common.exception

import io.csbroker.apiserver.common.enums.ErrorCode

class EntityNotFoundException(log: String) : BizException(ErrorCode.NOT_FOUND_ENTITY, log)
