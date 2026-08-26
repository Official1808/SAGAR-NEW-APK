package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val deviceId: Long,
    val tokenHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val revokedAt: Long? = null
)
