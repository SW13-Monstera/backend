package io.csbroker.apiserver.common.exception

import io.csbroker.apiserver.common.enums.ErrorCode

class OAuthProviderMissMatchException(log: String) : BizException(ErrorCode.PROVIDER_MISS_MATCH, log)
