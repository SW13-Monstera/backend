package com.csbroker.apiserver.common.auth

import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.common.util.log
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.UnsupportedJwtException
import java.security.Key
import java.util.Date
import java.util.UUID

const val AUTHORITIES_KEY: String = "ROLE"

class AuthToken(
    private var token: String,
    private val key: Key
) {
    constructor(id: UUID, expiry: Date, key: Key, role: Role? = null) : this("", key) {
        role?.let {
            this.token = this.createAuthToken(id, expiry, role)
        }.let {
            this.token = this.createAuthToken(id, expiry)
        }
    }

    val tokenClaims: Claims?
        get() {
            try {
                return Jwts.parserBuilder()
                    .setSigningKey(this.key)
                    .build()
                    .parseClaimsJws(this.token)
                    .body
            } catch (e: SecurityException) {
                log.error("Invalid JWT signature.")
            } catch (e: MalformedJwtException) {
                log.error("Invalid Jwt token.")
            } catch (e: ExpiredJwtException) {
                log.error("Expired JWT token.")
            } catch (e: UnsupportedJwtException) {
                log.error("Unsupported JWT token.")
            } catch (e: java.lang.IllegalArgumentException) {
                log.error("Jwt token compact of handler are invalid.")
            }
            return null
        }

    val expiredTokenClaims: Claims?
        get() {
            try {
                Jwts.parserBuilder()
                    .setSigningKey(this.key)
                    .build()
                    .parseClaimsJws(this.token)
                    .body
            } catch (e: ExpiredJwtException) {
                log.info("Expired JWT token.")
                return e.claims
            }
            return null
        }

    val isValid: Boolean
        get() = this.tokenClaims != null

    private fun createAuthToken(id: UUID, expiry: Date): String {
        return Jwts.builder()
            .setSubject(id.toString())
            .signWith(this.key, SignatureAlgorithm.HS256)
            .setExpiration(expiry)
            .compact()
    }

    private fun createAuthToken(id: UUID, expiry: Date, role: Role): String {
        return Jwts.builder()
            .setSubject(id.toString())
            .claim(AUTHORITIES_KEY, role)
            .signWith(this.key, SignatureAlgorithm.HS256)
            .setExpiration(expiry)
            .compact()
    }
}
