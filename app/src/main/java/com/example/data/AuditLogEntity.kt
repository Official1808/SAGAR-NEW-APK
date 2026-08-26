package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long? = null,
    val deviceId: Long? = null,
    val username: String? = null,
    val action: String,
    val ipAddress: String = "127.0.0.1",
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
