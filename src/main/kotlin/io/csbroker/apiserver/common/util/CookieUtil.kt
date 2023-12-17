package io.csbroker.apiserver.common.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Base64

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
    request.cookies?.find { cookie -> cookie.name == name }?.let { cookie ->
        cookie.value = ""
        cookie.path = "/"
        cookie.maxAge = 0
        response.addCookie(cookie)
    }
}

fun OAuth2AuthorizationRequest.serialize(): String {
    return Base64.getUrlEncoder()
        .encodeToString(this.toByteArray())
}

inline fun <reified T> Cookie.deserialize(): T {
    return Base64.getUrlDecoder().decode(this.value).let {
        ByteArrayInputStream(it).use { byteArrayInputStream ->
            ObjectInputStream(byteArrayInputStream).use { objectInput ->
                objectInput.readObject()
            }
        }
    } as? T ?: throw ClassCastException()
}

private fun Serializable.toByteArray(): ByteArray {
    return ByteArrayOutputStream().use {
        ObjectOutputStream(it).use { objectOutputStream ->
            objectOutputStream.writeObject(this)
            objectOutputStream.flush()
            it.toByteArray()
        }
    }
}
