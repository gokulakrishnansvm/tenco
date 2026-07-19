package com.tenco.backend.auth

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Stores OTP challenges + rate-limit counters. Backed by Redis in prod (shared across instances),
 * or an in-memory map in dev / single-instance deployments.
 */
interface OtpStore {
    fun saveCode(phone: String, code: String, ttlMs: Long)
    fun getCode(phone: String): String?
    fun clear(phone: String)
    /** Increments the request counter for [phone] within [windowMs] and returns the new count. */
    fun incrRequests(phone: String, windowMs: Long): Long
    /** Increments the verify-attempt counter for [phone] and returns the new count. */
    fun incrAttempts(phone: String): Long
}

class InMemoryOtpStore : OtpStore {
    private data class Entry(val value: String, val expiresAt: Long)
    private val codes = ConcurrentHashMap<String, Entry>()
    private val requests = ConcurrentHashMap<String, Entry>()
    private val attempts = ConcurrentHashMap<String, Int>()

    override fun saveCode(phone: String, code: String, ttlMs: Long) {
        codes[phone] = Entry(code, System.currentTimeMillis() + ttlMs)
    }

    override fun getCode(phone: String): String? {
        val e = codes[phone] ?: return null
        if (System.currentTimeMillis() > e.expiresAt) { codes.remove(phone); return null }
        return e.value
    }

    override fun clear(phone: String) {
        codes.remove(phone); attempts.remove(phone)
    }

    override fun incrRequests(phone: String, windowMs: Long): Long {
        val now = System.currentTimeMillis()
        val e = requests[phone]
        val count = if (e == null || now > e.expiresAt) 1 else e.value.toInt() + 1
        requests[phone] = Entry(count.toString(), if (e == null || now > e.expiresAt) now + windowMs else e.expiresAt)
        return count.toLong()
    }

    override fun incrAttempts(phone: String): Long =
        attempts.merge(phone, 1) { a, b -> a + b }!!.toLong()
}

class RedisOtpStore(private val redis: StringRedisTemplate) : OtpStore {
    private fun codeKey(p: String) = "otp:code:$p"
    private fun reqKey(p: String) = "otp:req:$p"
    private fun attKey(p: String) = "otp:att:$p"

    override fun saveCode(phone: String, code: String, ttlMs: Long) {
        redis.opsForValue().set(codeKey(phone), code, ttlMs, TimeUnit.MILLISECONDS)
    }

    override fun getCode(phone: String): String? = redis.opsForValue().get(codeKey(phone))

    override fun clear(phone: String) {
        redis.delete(codeKey(phone)); redis.delete(attKey(phone))
    }

    override fun incrRequests(phone: String, windowMs: Long): Long {
        val count = redis.opsForValue().increment(reqKey(phone)) ?: 1
        if (count == 1L) redis.expire(reqKey(phone), windowMs, TimeUnit.MILLISECONDS)
        return count
    }

    override fun incrAttempts(phone: String): Long {
        val count = redis.opsForValue().increment(attKey(phone)) ?: 1
        if (count == 1L) redis.expire(attKey(phone), 10, TimeUnit.MINUTES)
        return count
    }
}

@Configuration
class OtpStoreConfig {
    @Bean
    @ConditionalOnProperty(name = ["tenco.otp.store"], havingValue = "redis")
    fun redisOtpStore(redis: StringRedisTemplate): OtpStore = RedisOtpStore(redis)

    @Bean
    @ConditionalOnMissingBean(OtpStore::class)
    fun inMemoryOtpStore(): OtpStore = InMemoryOtpStore()
}
