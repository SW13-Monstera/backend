package com.csbroker.apiserver.auth

import com.csbroker.apiserver.common.util.log
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import java.security.Key
import java.util.Date

const val AUTHORITIES_KEY: String = "ROLE"

class AuthToken(
    var token: String,
    private val key: Key
) {
    constructor(email: String, expiry: Date, key: Key, role: String? = null) : this("", key) {
        if (role != null) {
            this.token = this.createAuthToken(email, expiry, role)
        } else {
            this.token = this.createAuthToken(email, expiry)
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
            } catch (e: SignatureException) {
                log.error("Jwt signature does not match.")
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
            } catch (e: SecurityException) {
                log.error("Invalid JWT signature.")
            } catch (e: MalformedJwtException) {
                log.error("Invalid Jwt token.")
            } catch (e: UnsupportedJwtException) {
                log.error("Unsupported JWT token.")
            } catch (e: java.lang.IllegalArgumentException) {
                log.error("Jwt token compact of handler are invalid.")
            } catch (e: SignatureException) {
                log.error("Jwt signature does not match.")
            }
            return null
        }

    val isValid: Boolean
        get() = this.tokenClaims != null

    private fun createAuthToken(email: String, expiry: Date): String {
        return Jwts.builder()
            .setSubject(email)
            .signWith(this.key, SignatureAlgorithm.HS256)
            .setExpiration(expiry)
            .compact()
    }

    private fun createAuthToken(email: String, expiry: Date, role: String): String {
        return Jwts.builder()
            .setSubject(email)
            .claim(AUTHORITIES_KEY, role)
            .signWith(this.key, SignatureAlgorithm.HS256)
            .setExpiration(expiry)
            .compact()
    }
}
