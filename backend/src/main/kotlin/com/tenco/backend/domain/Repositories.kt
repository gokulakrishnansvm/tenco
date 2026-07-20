package com.tenco.backend.domain

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<AppUser, String> {
    fun findByPhone(phone: String): AppUser?
}

interface DealerRepository : JpaRepository<Dealer, String> {
    fun findByUpdatedAtGreaterThan(cursor: Long): List<Dealer>
}

interface PurchaseRepository : JpaRepository<Purchase, String> {
    fun findByUpdatedAtGreaterThan(cursor: Long): List<Purchase>
}

interface VendorRepository : JpaRepository<Vendor, String> {
    fun findByPhoneEndingWith(suffix: String): Vendor?
    fun findByUpdatedAtGreaterThan(cursor: Long): List<Vendor>
}

interface PriceRepository : JpaRepository<Price, String> {
    fun findByVendorIdOrderByEffectiveFromDesc(vendorId: String): List<Price>
    fun findByUpdatedAtGreaterThan(cursor: Long): List<Price>
}

interface DeliveryRepository : JpaRepository<Delivery, String> {
    fun findByVendorId(vendorId: String): List<Delivery>
    fun findByUpdatedAtGreaterThan(cursor: Long): List<Delivery>
}

interface ComplaintRepository : JpaRepository<Complaint, String> {
    fun findByVendorId(vendorId: String): List<Complaint>
    fun findByUpdatedAtGreaterThan(cursor: Long): List<Complaint>
}

interface PaymentRepository : JpaRepository<Payment, String> {
    fun findByVendorId(vendorId: String): List<Payment>
    fun findByGatewayOrderId(orderId: String): Payment?
    fun findByUpdatedAtGreaterThan(cursor: Long): List<Payment>
}

interface DeviceTokenRepository : JpaRepository<DeviceToken, String> {
    fun findByUserId(userId: String): List<DeviceToken>
    fun findByToken(token: String): DeviceToken?
}

interface OrderRepository : JpaRepository<Order, String> {
    fun findByVendorId(vendorId: String): List<Order>
    fun findByUpdatedAtGreaterThan(cursor: Long): List<Order>
}
