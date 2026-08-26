package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- USERS ---
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE normalizedUsername = :normalizedUsername LIMIT 1")
    suspend fun getUserByNormalizedUsername(normalizedUsername: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'ADMIN' LIMIT 1")
    suspend fun getAdminUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET status = :status, approvedAt = :approvedAt WHERE id = :userId")
    suspend fun updateUserStatus(userId: Long, status: String, approvedAt: Long? = null)

    @Query("UPDATE users SET status = 'BLOCKED', blockedAt = :blockedAt WHERE id = :userId")
    suspend fun blockUser(userId: Long, blockedAt: Long = System.currentTimeMillis())

    @Query("UPDATE users SET status = 'APPROVED', blockedAt = null WHERE id = :userId")
    suspend fun unblockUser(userId: Long)

    @Query("UPDATE users SET lastLogin = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE users SET passwordHash = :newHash, salt = :newSalt WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newHash: String, newSalt: String)

    // --- DEVICES ---
    @Query("SELECT * FROM devices ORDER BY lastSeen DESC")
    fun getAllDevicesFlow(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE userId = :userId ORDER BY lastSeen DESC")
    fun getDevicesForUserFlow(userId: Long): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE userId = :userId AND secureDeviceIdentifier = :identifier LIMIT 1")
    suspend fun getDevice(userId: Long, identifier: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE id = :id LIMIT 1")
    suspend fun getDeviceById(id: Long): DeviceEntity?

    @Query("SELECT COUNT(*) FROM devices WHERE userId = :userId AND approvalStatus = 'APPROVED'")
    suspend fun getApprovedDeviceCountForUser(userId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity): Long

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Query("UPDATE devices SET approvalStatus = :status, lastSeen = :lastSeen WHERE id = :deviceId")
    suspend fun updateDeviceStatus(deviceId: Long, status: String, lastSeen: Long = System.currentTimeMillis())

    @Query("DELETE FROM devices WHERE id = :deviceId")
    suspend fun deleteDevice(deviceId: Long)

    // --- ACCESS REQUESTS ---
    @Query("SELECT * FROM access_requests ORDER BY createdAt DESC")
    fun getAllAccessRequestsFlow(): Flow<List<AccessRequestEntity>>

    @Query("SELECT * FROM access_requests WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingRequestsFlow(): Flow<List<AccessRequestEntity>>

    @Query("SELECT * FROM access_requests WHERE id = :id LIMIT 1")
    suspend fun getAccessRequestById(id: Long): AccessRequestEntity?

    @Query("SELECT * FROM access_requests WHERE approvalToken = :token LIMIT 1")
    suspend fun getRequestByApprovalToken(token: String): AccessRequestEntity?

    @Query("SELECT * FROM access_requests WHERE denyToken = :token LIMIT 1")
    suspend fun getRequestByDenyToken(token: String): AccessRequestEntity?

    @Query("SELECT * FROM access_requests WHERE userId = :userId AND status = 'PENDING' LIMIT 1")
    suspend fun getPendingRequestForUser(userId: Long): AccessRequestEntity?

    @Query("SELECT * FROM access_requests WHERE userId = :userId AND deviceIdentifier = :deviceIdentifier AND status = 'PENDING' LIMIT 1")
    suspend fun getPendingRequestForUserDevice(userId: Long, deviceIdentifier: String): AccessRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessRequest(request: AccessRequestEntity): Long

    @Update
    suspend fun updateAccessRequest(request: AccessRequestEntity)

    // --- SESSIONS ---
    @Query("SELECT * FROM sessions WHERE tokenHash = :tokenHash AND revokedAt IS NULL LIMIT 1")
    suspend fun getActiveSessionByTokenHash(tokenHash: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("UPDATE sessions SET revokedAt = :revokedAt WHERE tokenHash = :tokenHash")
    suspend fun revokeSession(tokenHash: String, revokedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sessions SET revokedAt = :revokedAt WHERE userId = :userId")
    suspend fun revokeAllUserSessions(userId: Long, revokedAt: Long = System.currentTimeMillis())

    // --- AUDIT LOGS ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 300")
    fun getAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE (userId = :userId OR username = :username) ORDER BY timestamp DESC")
    fun getUserAuditLogsFlow(userId: Long, username: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE (userId = :userId OR username = :username) ORDER BY timestamp DESC")
    suspend fun getUserAuditLogs(userId: Long, username: String): List<AuditLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    // --- EMAIL NOTIFICATIONS ---
    @Query("SELECT * FROM email_notifications ORDER BY timestamp DESC")
    fun getAllEmailNotificationsFlow(): Flow<List<EmailNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmailNotification(email: EmailNotificationEntity): Long

    @Update
    suspend fun updateEmailNotification(email: EmailNotificationEntity)

    @Query("UPDATE email_notifications SET status = 'ACTIONED', actionTaken = :action WHERE requestId = :requestId")
    suspend fun markEmailActioned(requestId: Long, action: String)

    // --- USER ASSIGNED LINKS ---
    @Query("SELECT * FROM user_links WHERE userId = :userId ORDER BY createdAt DESC")
    fun getLinksForUserFlow(userId: Long): Flow<List<UserLinkEntity>>

    @Query("SELECT * FROM user_links ORDER BY createdAt DESC")
    fun getAllUserLinksFlow(): Flow<List<UserLinkEntity>>

    @Query("SELECT * FROM user_links WHERE id = :linkId LIMIT 1")
    suspend fun getUserLinkById(linkId: Long): UserLinkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserLink(link: UserLinkEntity): Long

    @Update
    suspend fun updateUserLink(link: UserLinkEntity)

    @Query("DELETE FROM user_links WHERE id = :linkId")
    suspend fun deleteUserLink(linkId: Long)

    @Query("DELETE FROM user_links WHERE userId = :userId")
    suspend fun deleteLinksForUser(userId: Long)
}
