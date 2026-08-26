package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "email_notifications")
data class EmailNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val requestId: Long,
    val recipientEmail: String,
    val subject: String,
    val username: String,
    val requestType: String,
    val ipAddress: String,
    val region: String,
    val country: String,
    val deviceType: String,
    val operatingSystem: String,
    val appVersion: String,
    val deviceIdentifier: String,
    val approvalToken: String,
    val denyToken: String,
    val tokenExpiresAt: Long,
    val status: String = "DELIVERED", // "DELIVERED", "ACTIONED", "EXPIRED"
    val actionTaken: String? = null,  // "APPROVED", "DENIED"
    val timestamp: Long = System.currentTimeMillis()
)
