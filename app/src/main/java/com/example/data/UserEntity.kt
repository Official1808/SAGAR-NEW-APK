package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["normalizedUsername"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,             // Display username, e.g. "Sagar"
    val normalizedUsername: String,   // Lowercase for case-insensitive matching, e.g. "sagar"
    val passwordHash: String,         // Salted PBKDF2/SHA-256 hash
    val salt: String,                 // 16-byte random hex salt
    val status: String,               // "PENDING", "APPROVED", "DENIED", "BLOCKED"
    val role: String = "USER",        // "USER", "ADMIN"
    val createdAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val blockedAt: Long? = null,
    val lastLogin: Long? = null
)
