package com.csbroker.apiserver.common.exception

import com.csbroker.apiserver.common.enums.ErrorCode

class OAuthProviderMissMatchException(log: String) : BizException(ErrorCode.PROVIDER_MISS_MATCH, log)
