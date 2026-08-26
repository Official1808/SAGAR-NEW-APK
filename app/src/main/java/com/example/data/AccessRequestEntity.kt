package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "access_requests")
data class AccessRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val deviceId: Long? = null,
    val username: String,
    val requestType: String,            // "SIGNUP", "DEVICE_LOGIN"
    val status: String,                 // "PENDING", "APPROVED", "DENIED"
    val ipAddress: String,
    val approximateRegion: String,
    val deviceType: String,
    val operatingSystem: String,
    val deviceIdentifier: String,
    val approvalToken: String,          // Secure random token for email 1-click approve
    val denyToken: String,              // Secure random token for email 1-click deny
    val tokenExpiresAt: Long,           // Expiry timestamp
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val processedBy: String? = null
)
