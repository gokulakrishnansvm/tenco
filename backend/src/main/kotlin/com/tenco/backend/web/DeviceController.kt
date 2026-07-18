package com.tenco.backend.web

import com.tenco.backend.domain.DeviceToken
import com.tenco.backend.domain.DeviceTokenRepository
import com.tenco.backend.domain.now
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class DeviceRegisterBody(val userId: String, val fcmToken: String)

@RestController
@RequestMapping("/api/devices")
class DeviceController(private val devices: DeviceTokenRepository) {

    /** Upserts a device's FCM token (unique per token). */
    @PostMapping("/register")
    fun register(@RequestBody body: DeviceRegisterBody): DeviceToken {
        val existing = devices.findByToken(body.fcmToken)
        val entity = existing?.apply { userId = body.userId; updatedAt = now() }
            ?: DeviceToken(userId = body.userId, token = body.fcmToken)
        return devices.save(entity)
    }
}
