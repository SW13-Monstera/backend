package com.csbroker.apiserver.common.filter

import org.springframework.util.StreamUtils
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

class RequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    private var cachedInputStream : ByteArray

    init {
        val reqInputStream = request.inputStream
        this.cachedInputStream = StreamUtils.copyToByteArray(reqInputStream)
    }

    override fun getInputStream(): ServletInputStream {
        return object : ServletInputStream() {
            private var cachedBodyInputStream = ByteArrayInputStream(cachedInputStream)

            override fun read(): Int {
                return cachedBodyInputStream.read()
            }

            override fun isFinished(): Boolean {
                try {
                    return cachedBodyInputStream.available() == 0
                } catch (e: IOException){
                    e.printStackTrace()
                }
                return false
            }

            override fun isReady(): Boolean {
                return true
            }

            override fun setReadListener(listener: ReadListener?) {
                throw java.lang.UnsupportedOperationException()
            }
        }
    }

}
