package com.example.data

import com.example.security.EmailService
import com.example.security.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

sealed class LoginResult {
    data class Success(val user: UserEntity, val sessionToken: String) : LoginResult()
    data class UserPending(val username: String, val message: String) : LoginResult()
    data class UserDenied(val message: String) : LoginResult()
    data class UserBlocked(val message: String) : LoginResult()
    data class DevicePending(val username: String, val deviceId: String, val message: String) : LoginResult()
    data class DeviceDenied(val message: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

sealed class SignupResult {
    data class Success(val username: String, val message: String) : SignupResult()
    data class Error(val message: String) : SignupResult()
}

class GateRepository(
    private val db: AppDatabase,
    private val deviceManager: DeviceManager,
    private val emailService: EmailService? = null
) {
    private val dao = db.appDao()

    companion object {
        const val DESTINATION_URL = "https://pwthor.live/study/batches/6a71c66dd725a5817601a170"
        const val DEFAULT_ADMIN_USERNAME = "SAGAR"
        const val DEFAULT_ADMIN_PASSWORD = "RAJANI"
        const val ADMIN_EMAIL = EmailService.DEFAULT_ADMIN_EMAIL
    }

    suspend fun initializeDefaults() = withContext(Dispatchers.IO) {
        val existingAdmin = dao.getAdminUser()
        if (existingAdmin == null) {
            val salt = SecurityUtils.generateSalt()
            val hash = SecurityUtils.hashPassword(DEFAULT_ADMIN_PASSWORD, salt)
            val adminUser = UserEntity(
                username = DEFAULT_ADMIN_USERNAME,
                normalizedUsername = SecurityUtils.normalizeUsername(DEFAULT_ADMIN_USERNAME),
                passwordHash = hash,
                salt = salt,
                status = "APPROVED",
                role = "ADMIN",
                approvedAt = System.currentTimeMillis()
            )
            dao.insertUser(adminUser)
            dao.insertAuditLog(
                AuditLogEntity(
                    username = DEFAULT_ADMIN_USERNAME,
                    action = "SYSTEM_INITIALIZED",
                    details = "Master Administrator account initialized."
                )
            )
        }
    }

    // --- Flows ---
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsersFlow()
    val allDevices: Flow<List<DeviceEntity>> = dao.getAllDevicesFlow()
    val pendingRequests: Flow<List<AccessRequestEntity>> = dao.getPendingRequestsFlow()
    val allAccessRequests: Flow<List<AccessRequestEntity>> = dao.getAllAccessRequestsFlow()
    val auditLogs: Flow<List<AuditLogEntity>> = dao.getAuditLogsFlow()
    val emailNotifications: Flow<List<EmailNotificationEntity>> = dao.getAllEmailNotificationsFlow()

    fun getUserAuditLogsFlow(userId: Long, username: String): Flow<List<AuditLogEntity>> =
        dao.getUserAuditLogsFlow(userId, username)

    suspend fun getUserAuditLogs(userId: Long, username: String): List<AuditLogEntity> = withContext(Dispatchers.IO) {
        dao.getUserAuditLogs(userId, username)
    }

    // --- User Signup ---
    suspend fun signup(username: String, password: String): SignupResult = withContext(Dispatchers.IO) {
        val trimmed = username.trim()
        if (trimmed.length < 3) {
            return@withContext SignupResult.Error("Username must be at least 3 characters long.")
        }
        if (password.length < 4) {
            return@withContext SignupResult.Error("Password must be at least 4 characters long.")
        }

        val normalized = SecurityUtils.normalizeUsername(trimmed)
        val existing = dao.getUserByNormalizedUsername(normalized)
        if (existing != null) {
            return@withContext SignupResult.Error("Username is already registered. Please login or choose another username.")
        }

        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(password, salt)

        val newUser = UserEntity(
            username = trimmed,
            normalizedUsername = normalized,
            passwordHash = hash,
            salt = salt,
            status = "PENDING"
        )
        val userId = dao.insertUser(newUser)

        val approveToken = SecurityUtils.generateSecureToken()
        val denyToken = SecurityUtils.generateSecureToken()
        val tokenExpiry = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)

        val request = AccessRequestEntity(
            userId = userId,
            username = trimmed,
            requestType = "SIGNUP",
            status = "PENDING",
            ipAddress = deviceManager.getCurrentIP(),
            approximateRegion = "${deviceManager.getCurrentRegion()}, ${deviceManager.getCurrentCountry()}",
            deviceType = deviceManager.getCurrentDeviceType(),
            operatingSystem = deviceManager.getCurrentOS(),
            deviceIdentifier = deviceManager.getCurrentDeviceIdentifier(),
            approvalToken = approveToken,
            denyToken = denyToken,
            tokenExpiresAt = tokenExpiry
        )
        val reqId = dao.insertAccessRequest(request)

        val targetEmail = emailService?.adminEmail ?: ADMIN_EMAIL

        // Generate email alert record for administrator
        val email = EmailNotificationEntity(
            requestId = reqId,
            recipientEmail = targetEmail,
            subject = "PW SARA Security Alert: New Signup Request ($trimmed)",
            username = trimmed,
            requestType = "SIGNUP",
            ipAddress = deviceManager.getCurrentIP(),
            region = deviceManager.getCurrentRegion(),
            country = deviceManager.getCurrentCountry(),
            deviceType = deviceManager.getCurrentDeviceType(),
            operatingSystem = deviceManager.getCurrentOS(),
            appVersion = deviceManager.getAppVersion(),
            deviceIdentifier = deviceManager.getCurrentDeviceIdentifier(),
            approvalToken = approveToken,
            denyToken = denyToken,
            tokenExpiresAt = tokenExpiry
        )
        dao.insertEmailNotification(email)

        dao.insertAuditLog(
            AuditLogEntity(
                userId = userId,
                username = trimmed,
                action = "SIGNUP_REQUEST",
                ipAddress = deviceManager.getCurrentIP(),
                details = "User signup request submitted from device ${deviceManager.getCurrentDeviceType()} (${deviceManager.getCurrentDeviceIdentifier()})"
            )
        )

        // Asynchronously dispatch real email if emailService is present
        emailService?.let { es ->
            CoroutineScope(Dispatchers.IO).launch {
                val sendResult = es.sendAccessRequestEmail(
                    username = trimmed,
                    requestType = "SIGNUP",
                    deviceType = deviceManager.getCurrentDeviceType(),
                    operatingSystem = deviceManager.getCurrentOS(),
                    ipAddress = deviceManager.getCurrentIP(),
                    region = "${deviceManager.getCurrentRegion()}, ${deviceManager.getCurrentCountry()}",
                    approvalToken = approveToken,
                    denyToken = denyToken
                )
                if (sendResult.isSuccess) {
                    dao.insertAuditLog(
                        AuditLogEntity(
                            userId = userId,
                            username = trimmed,
                            action = "EMAIL_SENT",
                            ipAddress = deviceManager.getCurrentIP(),
                            details = "Security authorization email sent to $targetEmail with Approve/Deny actions"
                        )
                    )
                } else {
                    dao.insertAuditLog(
                        AuditLogEntity(
                            userId = userId,
                            username = trimmed,
                            action = "EMAIL_QUEUED",
                            ipAddress = deviceManager.getCurrentIP(),
                            details = "Email queued for admin: ${sendResult.exceptionOrNull()?.message ?: "SMTP Pending"}"
                        )
                    )
                }
            }
        }

        SignupResult.Success(
            username = trimmed,
            message = "Your registration request has been sent to the administrator. Please wait for approval."
        )
    }

    // --- User Login ---
    suspend fun login(username: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        val normalized = SecurityUtils.normalizeUsername(username)
        val user = dao.getUserByNormalizedUsername(normalized)

        if (user == null) {
            dao.insertAuditLog(
                AuditLogEntity(
                    username = username,
                    action = "LOGIN_FAILED",
                    ipAddress = deviceManager.getCurrentIP(),
                    details = "Login failed: Unknown username '$username'"
                )
            )
            return@withContext LoginResult.Error("Invalid username or password.")
        }

        // Verify password (case-sensitive)
        val passwordMatches = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
        if (!passwordMatches) {
            dao.insertAuditLog(
                AuditLogEntity(
                    userId = user.id,
                    username = user.username,
                    action = "LOGIN_FAILED",
                    ipAddress = deviceManager.getCurrentIP(),
                    details = "Login failed: Incorrect password attempt"
                )
            )
            return@withContext LoginResult.Error("Invalid username or password.")
        }

        // Check user status
        when (user.status) {
            "BLOCKED" -> {
                dao.insertAuditLog(
                    AuditLogEntity(
                        userId = user.id,
                        username = user.username,
                        action = "LOGIN_BLOCKED",
                        ipAddress = deviceManager.getCurrentIP(),
                        details = "Blocked user attempted login"
                    )
                )
                return@withContext LoginResult.UserBlocked("Your account has been blocked. Please contact the administrator.")
            }
            "DENIED" -> {
                return@withContext LoginResult.UserDenied("Your request was denied by the administrator.")
            }
            "PENDING" -> {
                return@withContext LoginResult.UserPending(
                    user.username,
                    "Your account is waiting for administrator approval."
                )
            }
            "APPROVED" -> {
                val currentDeviceIdentifier = deviceManager.getCurrentDeviceIdentifier()
                var device = dao.getDevice(user.id, currentDeviceIdentifier)

                if (device == null) {
                    // New/unregistered device -> register as PENDING and create device access request
                    val newDevice = DeviceEntity(
                        userId = user.id,
                        secureDeviceIdentifier = currentDeviceIdentifier,
                        deviceType = deviceManager.getCurrentDeviceType(),
                        operatingSystem = deviceManager.getCurrentOS(),
                        appVersion = deviceManager.getAppVersion(),
                        ipAddress = deviceManager.getCurrentIP(),
                        approximateRegion = "${deviceManager.getCurrentRegion()}, ${deviceManager.getCurrentCountry()}",
                        approvalStatus = "PENDING"
                    )
                    val deviceId = dao.insertDevice(newDevice)

                    val approveToken = SecurityUtils.generateSecureToken()
                    val denyToken = SecurityUtils.generateSecureToken()
                    val tokenExpiry = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)

                    val req = AccessRequestEntity(
                        userId = user.id,
                        deviceId = deviceId,
                        username = user.username,
                        requestType = "DEVICE_LOGIN",
                        status = "PENDING",
                        ipAddress = deviceManager.getCurrentIP(),
                        approximateRegion = "${deviceManager.getCurrentRegion()}, ${deviceManager.getCurrentCountry()}",
                        deviceType = deviceManager.getCurrentDeviceType(),
                        operatingSystem = deviceManager.getCurrentOS(),
                        deviceIdentifier = currentDeviceIdentifier,
                        approvalToken = approveToken,
                        denyToken = denyToken,
                        tokenExpiresAt = tokenExpiry
                    )
                    val reqId = dao.insertAccessRequest(req)

                    val targetEmail = emailService?.adminEmail ?: ADMIN_EMAIL

                    // Dispatch security email record
                    val email = EmailNotificationEntity(
                        requestId = reqId,
                        recipientEmail = targetEmail,
                        subject = "PW SARA Security Alert: New Device Login Request (${user.username})",
                        username = user.username,
                        requestType = "DEVICE_LOGIN",
                        ipAddress = deviceManager.getCurrentIP(),
                        region = deviceManager.getCurrentRegion(),
                        country = deviceManager.getCurrentCountry(),
                        deviceType = deviceManager.getCurrentDeviceType(),
                        operatingSystem = deviceManager.getCurrentOS(),
                        appVersion = deviceManager.getAppVersion(),
                        deviceIdentifier = currentDeviceIdentifier,
                        approvalToken = approveToken,
                        denyToken = denyToken,
                        tokenExpiresAt = tokenExpiry
                    )
                    dao.insertEmailNotification(email)

                    dao.insertAuditLog(
                        AuditLogEntity(
                            userId = user.id,
                            deviceId = deviceId,
                            username = user.username,
                            action = "DEVICE_LOGIN_REQUEST",
                            ipAddress = deviceManager.getCurrentIP(),
                            details = "New device login attempt on unregistered device ${deviceManager.getCurrentDeviceType()} ($currentDeviceIdentifier)"
                        )
                    )

                    // Dispatch email live
                    emailService?.let { es ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val sendResult = es.sendAccessRequestEmail(
                                username = user.username,
                                requestType = "DEVICE_LOGIN",
                                deviceType = deviceManager.getCurrentDeviceType(),
                                operatingSystem = deviceManager.getCurrentOS(),
                                ipAddress = deviceManager.getCurrentIP(),
                                region = "${deviceManager.getCurrentRegion()}, ${deviceManager.getCurrentCountry()}",
                                approvalToken = approveToken,
                                denyToken = denyToken
                            )
                            if (sendResult.isSuccess) {
                                dao.insertAuditLog(
                                    AuditLogEntity(
                                        userId = user.id,
                                        deviceId = deviceId,
                                        username = user.username,
                                        action = "EMAIL_SENT",
                                        ipAddress = deviceManager.getCurrentIP(),
                                        details = "Device approval email sent to $targetEmail with Approve/Deny buttons"
                                    )
                                )
                            }
                        }
                    }

                    return@withContext LoginResult.DevicePending(
                        user.username,
                        currentDeviceIdentifier,
                        "This device is not approved yet. A request has been sent to the administrator."
                    )
                }

                // Device exists in DB, check approval status
                when (device.approvalStatus) {
                    "PENDING" -> {
                        return@withContext LoginResult.DevicePending(
                            user.username,
                            currentDeviceIdentifier,
                            "This device is not approved yet. A request has been sent to the administrator."
                        )
                    }
                    "DENIED" -> {
                        return@withContext LoginResult.DeviceDenied("Your device access request was denied by the administrator.")
                    }
                    "REVOKED" -> {
                        return@withContext LoginResult.DeviceDenied("Access for this device was revoked by the administrator.")
                    }
                    "APPROVED" -> {
                        // User approved + Device approved -> Generate session
                        dao.updateLastLogin(user.id)
                        dao.updateDeviceStatus(device.id, "APPROVED")

                        val sessionToken = SecurityUtils.generateSecureToken(32)
                        val session = SessionEntity(
                            userId = user.id,
                            deviceId = device.id,
                            tokenHash = sessionToken,
                            expiresAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12)
                        )
                        dao.insertSession(session)

                        dao.insertAuditLog(
                            AuditLogEntity(
                                userId = user.id,
                                deviceId = device.id,
                                username = user.username,
                                action = "LOGIN_SUCCESS",
                                ipAddress = deviceManager.getCurrentIP(),
                                details = "Authentication successful on ${deviceManager.getCurrentDeviceType()}. Portal session initialized."
                            )
                        )

                        return@withContext LoginResult.Success(user, sessionToken)
                    }
                    else -> return@withContext LoginResult.Error("Unknown device approval status.")
                }
            }
            else -> return@withContext LoginResult.Error("Unknown account status.")
        }
    }

    // --- Admin Authentication ---
    suspend fun adminLogin(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val normalized = SecurityUtils.normalizeUsername(username)
        val admin = dao.getUserByNormalizedUsername(normalized) ?: return@withContext false
        if (admin.role != "ADMIN") return@withContext false

        val matches = SecurityUtils.verifyPassword(password, admin.salt, admin.passwordHash)
        if (matches) {
            dao.insertAuditLog(
                AuditLogEntity(
                    userId = admin.id,
                    username = admin.username,
                    action = "ADMIN_LOGIN_SUCCESS",
                    ipAddress = deviceManager.getCurrentIP(),
                    details = "Administrator logged into Admin Security Portal"
                )
            )
            true
        } else {
            dao.insertAuditLog(
                AuditLogEntity(
                    userId = admin.id,
                    username = admin.username,
                    action = "ADMIN_LOGIN_FAILED",
                    ipAddress = deviceManager.getCurrentIP(),
                    details = "Failed administrator login attempt"
                )
            )
            false
        }
    }

    // --- Admin Approval & Management Functions ---
    suspend fun approveUser(userId: Long) = withContext(Dispatchers.IO) {
        val user = dao.getUserById(userId) ?: return@withContext
        dao.updateUserStatus(userId, "APPROVED", System.currentTimeMillis())

        val req = dao.getPendingRequestForUser(userId)
        if (req != null) {
            dao.updateAccessRequest(req.copy(status = "APPROVED", processedAt = System.currentTimeMillis(), processedBy = "ADMIN"))
            dao.markEmailActioned(req.id, "APPROVED")
        }

        dao.insertAuditLog(
            AuditLogEntity(
                userId = userId,
                username = user.username,
                action = "USER_APPROVED",
                details = "User '${user.username}' approved by administrator"
            )
        )
    }

    suspend fun denyUser(userId: Long) = withContext(Dispatchers.IO) {
        val user = dao.getUserById(userId) ?: return@withContext
        dao.updateUserStatus(userId, "DENIED")

        val req = dao.getPendingRequestForUser(userId)
        if (req != null) {
            dao.updateAccessRequest(req.copy(status = "DENIED", processedAt = System.currentTimeMillis(), processedBy = "ADMIN"))
            dao.markEmailActioned(req.id, "DENIED")
        }

        dao.insertAuditLog(
            AuditLogEntity(
                userId = userId,
                username = user.username,
                action = "USER_DENIED",
                details = "User '${user.username}' denied by administrator"
            )
        )
    }

    suspend fun blockUser(userId: Long) = withContext(Dispatchers.IO) {
        val user = dao.getUserById(userId) ?: return@withContext
        dao.blockUser(userId)
        dao.revokeAllUserSessions(userId)

        dao.insertAuditLog(
            AuditLogEntity(
                userId = userId,
                username = user.username,
                action = "USER_BLOCKED",
                details = "User '${user.username}' blocked and all active sessions revoked by administrator"
            )
        )
    }

    suspend fun unblockUser(userId: Long) = withContext(Dispatchers.IO) {
        val user = dao.getUserById(userId) ?: return@withContext
        dao.unblockUser(userId)

        dao.insertAuditLog(
            AuditLogEntity(
                userId = userId,
                username = user.username,
                action = "USER_UNBLOCKED",
                details = "User '${user.username}' unblocked by administrator"
            )
        )
    }

    suspend fun approveDevice(deviceId: Long) = withContext(Dispatchers.IO) {
        val device = dao.getDeviceById(deviceId) ?: return@withContext
        dao.updateDeviceStatus(deviceId, "APPROVED")

        val reqs = dao.getPendingRequestForUserDevice(device.userId, device.secureDeviceIdentifier)
        if (reqs != null) {
            dao.updateAccessRequest(reqs.copy(status = "APPROVED", processedAt = System.currentTimeMillis(), processedBy = "ADMIN"))
            dao.markEmailActioned(reqs.id, "APPROVED")
        }

        val user = dao.getUserById(device.userId)
        dao.insertAuditLog(
            AuditLogEntity(
                userId = device.userId,
                deviceId = device.id,
                username = user?.username,
                action = "DEVICE_APPROVED",
                details = "Device '${device.deviceType}' (${device.secureDeviceIdentifier}) approved for user '${user?.username}'"
            )
        )
    }

    suspend fun denyDevice(deviceId: Long) = withContext(Dispatchers.IO) {
        val device = dao.getDeviceById(deviceId) ?: return@withContext
        dao.updateDeviceStatus(deviceId, "DENIED")

        val reqs = dao.getPendingRequestForUserDevice(device.userId, device.secureDeviceIdentifier)
        if (reqs != null) {
            dao.updateAccessRequest(reqs.copy(status = "DENIED", processedAt = System.currentTimeMillis(), processedBy = "ADMIN"))
            dao.markEmailActioned(reqs.id, "DENIED")
        }

        val user = dao.getUserById(device.userId)
        dao.insertAuditLog(
            AuditLogEntity(
                userId = device.userId,
                deviceId = device.id,
                username = user?.username,
                action = "DEVICE_DENIED",
                details = "Device '${device.deviceType}' (${device.secureDeviceIdentifier}) denied for user '${user?.username}'"
            )
        )
    }

    suspend fun revokeDevice(deviceId: Long) = withContext(Dispatchers.IO) {
        val device = dao.getDeviceById(deviceId) ?: return@withContext
        dao.updateDeviceStatus(deviceId, "REVOKED")

        val user = dao.getUserById(device.userId)
        dao.insertAuditLog(
            AuditLogEntity(
                userId = device.userId,
                deviceId = device.id,
                username = user?.username,
                action = "DEVICE_REVOKED",
                details = "Device '${device.deviceType}' (${device.secureDeviceIdentifier}) revoked by administrator"
            )
        )
    }

    suspend fun removeDevice(deviceId: Long) = withContext(Dispatchers.IO) {
        val device = dao.getDeviceById(deviceId) ?: return@withContext
        dao.deleteDevice(deviceId)

        val user = dao.getUserById(device.userId)
        dao.insertAuditLog(
            AuditLogEntity(
                userId = device.userId,
                deviceId = device.id,
                username = user?.username,
                action = "DEVICE_REMOVED",
                details = "Device '${device.deviceType}' deleted from registered records"
            )
        )
    }

    // --- Signed Token Processing (Email Link Actions) ---
    suspend fun processEmailToken(token: String): Result<String> = withContext(Dispatchers.IO) {
        var isApprove = true
        var request = dao.getRequestByApprovalToken(token)
        if (request == null) {
            isApprove = false
            request = dao.getRequestByDenyToken(token)
        }

        if (request == null) {
            return@withContext Result.failure(Exception("Invalid action token. Request not found."))
        }

        if (System.currentTimeMillis() > request.tokenExpiresAt) {
            return@withContext Result.failure(Exception("This approval link has expired (24h time-to-live elapsed)."))
        }

        if (request.status != "PENDING") {
            return@withContext Result.failure(Exception("This request has already been processed with status: ${request.status}."))
        }

        if (isApprove) {
            if (request.requestType == "SIGNUP") {
                approveUser(request.userId)
            } else if (request.requestType == "DEVICE_LOGIN") {
                request.deviceId?.let { approveDevice(it) }
            }
            dao.updateAccessRequest(request.copy(status = "APPROVED", processedAt = System.currentTimeMillis(), processedBy = "EMAIL_LINK"))
            dao.markEmailActioned(request.id, "APPROVED")
            Result.success("Request for '${request.username}' successfully APPROVED via secure signed link.")
        } else {
            if (request.requestType == "SIGNUP") {
                denyUser(request.userId)
            } else if (request.requestType == "DEVICE_LOGIN") {
                request.deviceId?.let { denyDevice(it) }
            }
            dao.updateAccessRequest(request.copy(status = "DENIED", processedAt = System.currentTimeMillis(), processedBy = "EMAIL_LINK"))
            dao.markEmailActioned(request.id, "DENIED")
            Result.success("Request for '${request.username}' successfully DENIED via secure signed link.")
        }
    }

    // --- Admin Password Change ---
    suspend fun changeAdminPassword(oldPass: String, newPass: String): Result<String> = withContext(Dispatchers.IO) {
        val admin = dao.getAdminUser() ?: return@withContext Result.failure(Exception("Admin not found"))
        if (!SecurityUtils.verifyPassword(oldPass, admin.salt, admin.passwordHash)) {
            return@withContext Result.failure(Exception("Current admin password is incorrect."))
        }
        if (newPass.length < 5) {
            return@withContext Result.failure(Exception("New password must be at least 5 characters long."))
        }

        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPass, newSalt)
        dao.updatePassword(admin.id, newHash, newSalt)

        dao.insertAuditLog(
            AuditLogEntity(
                userId = admin.id,
                username = admin.username,
                action = "ADMIN_PASSWORD_CHANGED",
                details = "Administrator password was securely updated."
            )
        )
        Result.success("Admin password updated successfully.")
    }

    // --- Session Logout ---
    suspend fun logout(sessionToken: String?) = withContext(Dispatchers.IO) {
        if (!sessionToken.isNullOrBlank()) {
            dao.revokeSession(sessionToken)
            dao.insertAuditLog(
                AuditLogEntity(
                    action = "LOGOUT",
                    details = "User logged out. Active authentication session destroyed."
                )
            )
        }
    }

    // --- Resend Email Notification ---
    suspend fun resendRequestEmail(requestId: Long): Result<String> = withContext(Dispatchers.IO) {
        val req = dao.getAccessRequestById(requestId) ?: return@withContext Result.failure(Exception("Request not found."))
        val es = emailService ?: return@withContext Result.failure(Exception("Email service not available."))
        val res = es.sendAccessRequestEmail(
            username = req.username,
            requestType = req.requestType,
            deviceType = req.deviceType,
            operatingSystem = req.operatingSystem,
            ipAddress = req.ipAddress,
            region = req.approximateRegion,
            approvalToken = req.approvalToken,
            denyToken = req.denyToken
        )
        if (res.isSuccess) {
            dao.insertAuditLog(
                AuditLogEntity(
                    userId = req.userId,
                    username = req.username,
                    action = "EMAIL_RESENT",
                    ipAddress = deviceManager.getCurrentIP(),
                    details = "Security authorization email re-dispatched to ${es.adminEmail}"
                )
            )
        }
        res
    }

    // --- User Custom Links Management ---
    val allUserLinks: Flow<List<UserLinkEntity>> = dao.getAllUserLinksFlow()

    fun getLinksForUser(userId: Long): Flow<List<UserLinkEntity>> = dao.getLinksForUserFlow(userId)

    suspend fun addUserLink(
        userId: Long,
        username: String,
        title: String,
        url: String,
        description: String = ""
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (title.isBlank()) {
            return@withContext Result.failure(Exception("Link title cannot be empty."))
        }
        if (url.isBlank()) {
            return@withContext Result.failure(Exception("URL cannot be empty."))
        }
        val cleanUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        val link = UserLinkEntity(
            userId = userId,
            username = username,
            title = title.trim(),
            url = cleanUrl.trim(),
            description = description.trim()
        )
        val id = dao.insertUserLink(link)
        dao.insertAuditLog(
            AuditLogEntity(
                userId = userId,
                username = username,
                action = "LINK_ADDED",
                details = "Admin added resource link '$title' for student '$username'"
            )
        )
        Result.success(id)
    }

    suspend fun deleteUserLink(linkId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val link = dao.getUserLinkById(linkId)
        dao.deleteUserLink(linkId)
        if (link != null) {
            dao.insertAuditLog(
                AuditLogEntity(
                    userId = link.userId,
                    username = link.username,
                    action = "LINK_DELETED",
                    details = "Admin removed resource link '${link.title}' from student '${link.username}'"
                )
            )
        }
        Result.success(Unit)
    }

    suspend fun updateUserLink(
        linkId: Long,
        title: String,
        url: String,
        description: String = ""
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = dao.getUserLinkById(linkId) ?: return@withContext Result.failure(Exception("Link not found."))
        val cleanUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        val updated = existing.copy(
            title = title.trim(),
            url = cleanUrl.trim(),
            description = description.trim()
        )
        dao.updateUserLink(updated)
        Result.success(Unit)
    }
}
