package io.csbroker.apiserver.common.filter

import io.csbroker.apiserver.common.util.log
import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.util.StreamUtils
import java.io.ByteArrayInputStream

class RequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    private var cachedInputStream: ByteArray

    init {
        val reqInputStream = request.inputStream
        cachedInputStream = StreamUtils.copyToByteArray(reqInputStream)
    }

    override fun getInputStream(): ServletInputStream {
        return object : ServletInputStream() {
            private val cachedBodyInputStream = ByteArrayInputStream(cachedInputStream)

            override fun read(): Int {
                return cachedBodyInputStream.read()
            }

            override fun isFinished() = runCatching {
                return cachedBodyInputStream.available() == 0
            }.onFailure {
                log.error(it.message)
            }.getOrDefault(false)

            override fun isReady(): Boolean {
                return true
            }

            override fun setReadListener(listener: ReadListener?) {
                throw java.lang.UnsupportedOperationException()
            }
        }
    }
}
