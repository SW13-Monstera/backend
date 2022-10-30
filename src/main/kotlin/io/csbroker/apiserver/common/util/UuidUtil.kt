package io.csbroker.apiserver.common.util

import java.nio.ByteBuffer
import java.util.UUID

fun uuidAsByte(uuid: UUID?): ByteArray? {
    if (uuid == null) {
        return null
    }
    val byteBufferWrapper = ByteBuffer.wrap(ByteArray(16))
    byteBufferWrapper.putLong(uuid.mostSignificantBits)
    byteBufferWrapper.putLong(uuid.leastSignificantBits)
    return byteBufferWrapper.array()
}
