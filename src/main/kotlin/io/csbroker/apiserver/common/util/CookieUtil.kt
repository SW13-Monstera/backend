package io.csbroker.apiserver.common.util

import org.springframework.util.SerializationUtils
import java.util.Base64
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

fun getCookie(request: HttpServletRequest, name: String) = request.cookies?.let {
    it.find { cookie -> cookie.name == name }
}

fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Long) {
    val cookie = Cookie(name, value)
    cookie.path = "/"
    cookie.isHttpOnly = true
    cookie.secure = true
    cookie.maxAge = maxAge.toInt()

    response.addCookie(cookie)
}

fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
    request.cookies?.let {
        it.find { cookie -> cookie.name == name }?.let { cookie ->
            cookie.value = ""
            cookie.path = "/"
            cookie.maxAge = 0
            response.addCookie(cookie)
        }
    }
}

fun serialize(obj: Any): String {
    return Base64.getUrlEncoder()
        .encodeToString(SerializationUtils.serialize(obj))
}

fun <T> deserialize(cookie: Cookie, cls: Class<T>): T {
    return cls.cast(
        SerializationUtils.deserialize(
            Base64.getUrlDecoder().decode(cookie.value),
        ),
    )
}
