package com.tenco.backend.auth

import com.tenco.backend.domain.AppUser
import com.tenco.backend.domain.UserRepository
import com.tenco.backend.security.JwtService
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class OtpRequestBody(@field:NotBlank val phone: String)
data class OtpRequestResponse(val sent: Boolean, val devOtp: String?)

data class OtpVerifyBody(
    @field:NotBlank val phone: String,
    @field:NotBlank val code: String,
    val name: String? = null,
    val role: String? = null, // SUPPLIER | VENDOR (used on first login)
)

data class AuthResponse(
    val userId: String,
    val role: String,
    val accessToken: String,
    val refreshToken: String,
)

@RestController
@RequestMapping("/auth")
class AuthController(
    private val otpService: OtpService,
    private val users: UserRepository,
    private val jwt: JwtService,
) {
    @PostMapping("/otp/request")
    fun requestOtp(@RequestBody body: OtpRequestBody): OtpRequestResponse {
        val otp = otpService.request(normalize(body.phone))
        return OtpRequestResponse(sent = true, devOtp = otp)
    }

    @PostMapping("/otp/verify")
    fun verifyOtp(@RequestBody body: OtpVerifyBody): AuthResponse {
        val phone = normalize(body.phone)
        if (!otpService.verify(phone, body.code)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired OTP")
        }
        val user = users.findByPhone(phone) ?: users.save(
            AppUser(
                phone = phone,
                name = body.name ?: "",
                role = (body.role ?: "VENDOR").uppercase(),
            )
        )
        val tokens = jwt.issue(user.id, user.phone, user.role)
        return AuthResponse(user.id, user.role, tokens.accessToken, tokens.refreshToken)
    }

    private fun normalize(phone: String) = phone.filter(Char::isDigit).takeLast(10)
}
