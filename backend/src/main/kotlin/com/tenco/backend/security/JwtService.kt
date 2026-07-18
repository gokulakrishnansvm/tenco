package com.tenco.backend.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

data class TokenPair(val accessToken: String, val refreshToken: String, val role: String)

@Service
class JwtService(
    @Value("\${tenco.jwt.secret}") secret: String,
    @Value("\${tenco.jwt.access-token-minutes}") private val accessMinutes: Long,
    @Value("\${tenco.jwt.refresh-token-days}") private val refreshDays: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun issue(userId: String, phone: String, role: String): TokenPair {
        val now = System.currentTimeMillis()
        val access = build(userId, phone, role, now, accessMinutes * 60_000)
        val refresh = build(userId, phone, role, now, refreshDays * 24 * 60 * 60_000)
        return TokenPair(access, refresh, role)
    }

    private fun build(userId: String, phone: String, role: String, now: Long, ttlMs: Long): String =
        Jwts.builder()
            .subject(userId)
            .claim("phone", phone)
            .claim("role", role)
            .issuedAt(Date(now))
            .expiration(Date(now + ttlMs))
            .signWith(key)
            .compact()

    /** Returns the parsed principal, or null if the token is invalid/expired. */
    fun parse(token: String): JwtPrincipal? = try {
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        JwtPrincipal(
            userId = claims.subject,
            phone = claims["phone"] as? String ?: "",
            role = claims["role"] as? String ?: "VENDOR",
        )
    } catch (e: Exception) {
        null
    }
}

data class JwtPrincipal(val userId: String, val phone: String, val role: String)
