package com.csbroker.apiserver.common.exception

class TokenValidFailedException(
    message: String = "Failed to generate Token."
) : RuntimeException(message)
