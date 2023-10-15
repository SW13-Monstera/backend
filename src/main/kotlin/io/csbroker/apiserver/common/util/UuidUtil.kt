package io.csbroker.apiserver.common.util

import java.nio.ByteBuffer
import java.util.UUID

fun UUID.asByte(): ByteArray? {
    val byteBufferWrapper = ByteBuffer.wrap(ByteArray(16))
    byteBufferWrapper.putLong(this.mostSignificantBits)
    byteBufferWrapper.putLong(this.leastSignificantBits)
    return byteBufferWrapper.array()
}
