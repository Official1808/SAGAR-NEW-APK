package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "devices",
    indices = [
        Index(value = ["userId", "secureDeviceIdentifier"], unique = true),
        Index(value = ["secureDeviceIdentifier"])
    ]
)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val secureDeviceIdentifier: String, // Application-specific unique device ID
    val deviceType: String,             // e.g. "Pixel 9 Pro", "Samsung Galaxy S24"
    val operatingSystem: String,        // e.g. "Android 15 (API 35)"
    val appVersion: String = "1.0.0",
    val ipAddress: String,              // e.g. "103.212.144.58"
    val approximateRegion: String,      // e.g. "New Delhi, India"
    val firstSeen: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val approvalStatus: String          // "PENDING", "APPROVED", "DENIED", "REVOKED"
)
