package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccessRequestEntity
import com.example.data.AppDatabase
import com.example.data.AuditLogEntity
import com.example.data.DeviceEntity
import com.example.data.DeviceManager
import com.example.data.DeviceProfile
import com.example.data.EmailNotificationEntity
import com.example.data.GateRepository
import com.example.data.LoginResult
import com.example.data.SignupResult
import com.example.data.UserEntity
import com.example.data.UserLinkEntity
import com.example.security.EmailService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    USER_AUTH,
    USER_PENDING,
    DEVICE_PENDING,
    USER_DENIED,
    DEVICE_DENIED,
    USER_BLOCKED,
    DESTINATION,
    ADMIN_LOGIN,
    ADMIN_DASHBOARD
}

enum class AdminTab {
    OVERVIEW,
    PENDING_APPROVALS,
    USERS,
    USER_HISTORY,
    DEVICES,
    EMAIL_ALERTS,
    AUDIT_LOGS,
    SETTINGS
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val pendingUsername: String? = null,
    val pendingDeviceId: String? = null
)

class GateViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val deviceManager = DeviceManager(application)
    val emailService = EmailService(application)
    val repository = GateRepository(db, deviceManager, emailService)

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow(AppScreen.USER_AUTH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Auth UI State
    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    // Authenticated User Session
    private val _authenticatedUser = MutableStateFlow<UserEntity?>(null)
    val authenticatedUser: StateFlow<UserEntity?> = _authenticatedUser.asStateFlow()

    private val _sessionToken = MutableStateFlow<String?>(null)
    val sessionToken: StateFlow<String?> = _sessionToken.asStateFlow()

    // Admin State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminTab = MutableStateFlow(AdminTab.OVERVIEW)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    // Selected User for Detailed History View
    private val _selectedUserForHistory = MutableStateFlow<UserEntity?>(null)
    val selectedUserForHistory: StateFlow<UserEntity?> = _selectedUserForHistory.asStateFlow()

    // Device Profile (Hardware Profile)
    val activeDeviceProfile: StateFlow<DeviceProfile> = deviceManager.activeProfile

    // Repository Flows
    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val devices: StateFlow<List<DeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRequests: StateFlow<List<AccessRequestEntity>> = repository.pendingRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccessRequests: StateFlow<List<AccessRequestEntity>> = repository.allAccessRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emailNotifications: StateFlow<List<EmailNotificationEntity>> = repository.emailNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUserLinks: StateFlow<List<UserLinkEntity>> = repository.allUserLinks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val linksForCurrentUser: StateFlow<List<UserLinkEntity>> = _authenticatedUser
        .flatMapLatest { u ->
            if (u != null) repository.getLinksForUser(u.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedUserLinks: StateFlow<List<UserLinkEntity>> = _selectedUserForHistory
        .flatMapLatest { u ->
            if (u != null) repository.getLinksForUser(u.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedUserAuditLogs: StateFlow<List<AuditLogEntity>> = _selectedUserForHistory
        .flatMapLatest { u ->
            if (u != null) repository.getUserAuditLogsFlow(u.id, u.username)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeDefaults()
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        _authUiState.value = _authUiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun setAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun selectUserForHistory(user: UserEntity?) {
        _selectedUserForHistory.value = user
        if (user != null) {
            _adminTab.value = AdminTab.USER_HISTORY
        }
    }

    fun clearMessages() {
        _authUiState.value = _authUiState.value.copy(errorMessage = null, successMessage = null)
    }

    // --- User Actions ---
    fun userSignup(username: String, password: String, confirmPass: String) {
        if (password != confirmPass) {
            _authUiState.value = _authUiState.value.copy(errorMessage = "Passwords do not match.")
            return
        }
        _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = repository.signup(username, password)) {
                is SignupResult.Success -> {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        pendingUsername = result.username,
                        successMessage = result.message
                    )
                    _currentScreen.value = AppScreen.USER_PENDING
                }
                is SignupResult.Error -> {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun userLogin(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authUiState.value = _authUiState.value.copy(errorMessage = "Please enter both username and password.")
            return
        }
        _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = repository.login(username, password)) {
                is LoginResult.Success -> {
                    _authenticatedUser.value = result.user
                    _sessionToken.value = result.sessionToken
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        errorMessage = null,
                        successMessage = "Authentication successful!"
                    )
                    _currentScreen.value = AppScreen.DESTINATION
                }
                is LoginResult.UserPending -> {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        pendingUsername = result.username,
                        errorMessage = result.message
                    )
                    _currentScreen.value = AppScreen.USER_PENDING
                }
                is LoginResult.DevicePending -> {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        pendingUsername = result.username,
                        pendingDeviceId = result.deviceId,
                        errorMessage = result.message
                    )
                    _currentScreen.value = AppScreen.DEVICE_PENDING
                }
                is LoginResult.UserDenied -> {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    _currentScreen.value = AppScreen.USER_DENIED
                }
                is LoginResult.DeviceDenied -> {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    _currentScreen.value = AppScreen.DEVICE_DENIED
                }
                is LoginResult.UserBlocked -> {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    _currentScreen.value = AppScreen.USER_BLOCKED
                }
                is LoginResult.Error -> {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun userLogout() {
        viewModelScope.launch {
            val token = _sessionToken.value
            repository.logout(token)
            _authenticatedUser.value = null
            _sessionToken.value = null
            _authUiState.value = AuthUiState(successMessage = "Logged out successfully.")
            _currentScreen.value = AppScreen.USER_AUTH
        }
    }

    // --- Admin Actions ---
    fun adminLogin(username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) {
            _authUiState.value = _authUiState.value.copy(errorMessage = "Please enter admin credentials.")
            return
        }
        _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val success = repository.adminLogin(username, pass)
            if (success) {
                _isAdminLoggedIn.value = true
                _authUiState.value = _authUiState.value.copy(isLoading = false, errorMessage = null)
                _currentScreen.value = AppScreen.ADMIN_DASHBOARD
            } else {
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = "Invalid administrator username or password."
                )
            }
        }
    }

    fun adminLogout() {
        _isAdminLoggedIn.value = false
        _currentScreen.value = AppScreen.USER_AUTH
        _authUiState.value = AuthUiState(successMessage = "Admin signed out.")
    }

    fun approveUser(userId: Long) {
        viewModelScope.launch {
            repository.approveUser(userId)
            _authUiState.value = _authUiState.value.copy(successMessage = "User approved successfully.")
        }
    }

    fun denyUser(userId: Long) {
        viewModelScope.launch {
            repository.denyUser(userId)
            _authUiState.value = _authUiState.value.copy(successMessage = "User access denied.")
        }
    }

    fun blockUser(userId: Long) {
        viewModelScope.launch {
            repository.blockUser(userId)
            _authUiState.value = _authUiState.value.copy(successMessage = "User has been blocked.")
        }
    }

    fun unblockUser(userId: Long) {
        viewModelScope.launch {
            repository.unblockUser(userId)
            _authUiState.value = _authUiState.value.copy(successMessage = "User has been unblocked.")
        }
    }

    fun approveDevice(deviceId: Long) {
        viewModelScope.launch {
            repository.approveDevice(deviceId)
            _authUiState.value = _authUiState.value.copy(successMessage = "Device approved successfully.")
        }
    }

    fun denyDevice(deviceId: Long) {
        viewModelScope.launch {
            repository.denyDevice(deviceId)
            _authUiState.value = _authUiState.value.copy(successMessage = "Device access denied.")
        }
    }

    fun revokeDevice(deviceId: Long) {
        viewModelScope.launch {
            repository.revokeDevice(deviceId)
            _authUiState.value = _authUiState.value.copy(successMessage = "Device access revoked.")
        }
    }

    fun removeDevice(deviceId: Long) {
        viewModelScope.launch {
            repository.removeDevice(deviceId)
            _authUiState.value = _authUiState.value.copy(successMessage = "Device record deleted.")
        }
    }

    fun processEmailToken(token: String) {
        viewModelScope.launch {
            _authUiState.value = _authUiState.value.copy(isLoading = true)
            val result = repository.processEmailToken(token)
            result.onSuccess { msg ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    successMessage = msg,
                    errorMessage = null
                )
            }.onFailure { err ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Failed to process token action."
                )
            }
        }
    }

    fun changeAdminPassword(oldPass: String, newPass: String) {
        viewModelScope.launch {
            val result = repository.changeAdminPassword(oldPass, newPass)
            result.onSuccess { msg ->
                _authUiState.value = _authUiState.value.copy(successMessage = msg, errorMessage = null)
            }.onFailure { err ->
                _authUiState.value = _authUiState.value.copy(errorMessage = err.message ?: "Failed to update password.")
            }
        }
    }

    // --- Gmail & Resend Email Dispatcher & Vercel Webhook Settings ---
    val vercelBackendUrl: String get() = emailService.vercelBackendUrl

    fun updateGmailSettings(
        smtpSenderEmail: String,
        appPassword: String,
        recipientEmail: String = smtpSenderEmail,
        vercelBackendUrl: String = "",
        resendApiKey: String = ""
    ) {
        emailService.smtpSenderEmail = smtpSenderEmail
        emailService.appPassword = appPassword
        emailService.recipientEmail = recipientEmail
        if (vercelBackendUrl.isNotBlank()) {
            emailService.vercelBackendUrl = vercelBackendUrl
        }
        emailService.resendApiKey = resendApiKey
        _authUiState.value = _authUiState.value.copy(
            successMessage = "Email, Resend API & Vercel Gateway settings saved successfully."
        )
    }

    fun sendTestVerificationEmail() {
        viewModelScope.launch {
            _authUiState.value = _authUiState.value.copy(isLoading = true)
            val result = emailService.sendTestEmail()
            result.onSuccess { msg ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    successMessage = msg,
                    errorMessage = null
                )
            }.onFailure { err ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Failed to send test email. Please check your App Password."
                )
            }
        }
    }

    fun resendApprovalEmail(requestId: Long) {
        viewModelScope.launch {
            _authUiState.value = _authUiState.value.copy(isLoading = true)
            val result = repository.resendRequestEmail(requestId)
            result.onSuccess { msg ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    successMessage = msg,
                    errorMessage = null
                )
            }.onFailure { err ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Failed to resend email."
                )
            }
        }
    }

    // --- Admin User Links Management ---
    fun addUserLink(userId: Long, username: String, title: String, url: String, description: String = "") {
        viewModelScope.launch {
            _authUiState.value = _authUiState.value.copy(isLoading = true)
            val result = repository.addUserLink(userId, username, title, url, description)
            result.onSuccess {
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    successMessage = "Link '$title' successfully added for $username!",
                    errorMessage = null
                )
            }.onFailure { err ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Failed to add link."
                )
            }
        }
    }

    fun deleteUserLink(linkId: Long) {
        viewModelScope.launch {
            val result = repository.deleteUserLink(linkId)
            result.onSuccess {
                _authUiState.value = _authUiState.value.copy(
                    successMessage = "Resource link deleted successfully.",
                    errorMessage = null
                )
            }.onFailure { err ->
                _authUiState.value = _authUiState.value.copy(
                    errorMessage = err.message ?: "Failed to delete link."
                )
            }
        }
    }

    fun updateUserLink(linkId: Long, title: String, url: String, description: String = "") {
        viewModelScope.launch {
            val result = repository.updateUserLink(linkId, title, url, description)
            result.onSuccess {
                _authUiState.value = _authUiState.value.copy(
                    successMessage = "Resource link updated.",
                    errorMessage = null
                )
            }.onFailure { err ->
                _authUiState.value = _authUiState.value.copy(
                    errorMessage = err.message ?: "Failed to update link."
                )
            }
        }
    }

    // --- Vercel Status Check / Auto-Approval Polling ---
    fun checkVercelStatus(username: String?) {
        val targetUser = if (!username.isNullOrBlank()) {
            username
        } else {
            _authUiState.value.pendingUsername ?: ""
        }

        if (targetUser.isBlank()) return

        val vercelUrl = emailService.vercelBackendUrl.trimEnd('/')

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            var status = ""
            val cleanUser = targetUser.trim().lowercase()

            // 1. Check Vercel Backend endpoint (/api/check-status)
            if (vercelUrl.isNotBlank()) {
                try {
                    val encodedUser = java.net.URLEncoder.encode(cleanUser, "UTF-8")
                    val url = java.net.URL("$vercelUrl/api/check-status?username=$encodedUser&_t=${System.currentTimeMillis()}")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.useCaches = false
                    conn.setRequestProperty("Cache-Control", "no-cache")
                    conn.setRequestProperty("Pragma", "no-cache")
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    conn.setRequestProperty("Accept", "application/json")

                    if (conn.responseCode == 200) {
                        val response = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = org.json.JSONObject(response)
                        val s = json.optString("status", "").uppercase()
                        if (s.isNotBlank() && s != "PENDING") {
                            status = s
                        }
                    }
                    conn.disconnect()
                } catch (e: Exception) {
                    android.util.Log.w("GateViewModel", "Vercel check error: ${e.message}")
                }
            }

            // 2. Real-time Pub/Sub Check via ntfy.sh (Ultra fast & 100% reliable)
            if (status.isBlank() || status == "PENDING") {
                try {
                    val topic = "pwsara_auth_${cleanUser.replace(Regex("[^a-z0-9]"), "_")}"
                    val ntfyUrl = java.net.URL("https://ntfy.sh/$topic/raw?poll=1&_t=${System.currentTimeMillis()}")
                    val ntfyConn = ntfyUrl.openConnection() as java.net.HttpURLConnection
                    ntfyConn.requestMethod = "GET"
                    ntfyConn.useCaches = false
                    ntfyConn.connectTimeout = 3000
                    ntfyConn.readTimeout = 3000

                    if (ntfyConn.responseCode == 200) {
                        val raw = ntfyConn.inputStream.bufferedReader().use { it.readText() }
                        if (raw.contains("APPROVED", ignoreCase = true)) {
                            status = "APPROVED"
                        } else if (raw.contains("DENIED", ignoreCase = true)) {
                            status = "DENIED"
                        }
                    }
                    ntfyConn.disconnect()
                } catch (e: Exception) {
                    android.util.Log.w("GateViewModel", "ntfy check error: ${e.message}")
                }
            }

            // 3. Direct Cloud KV Fallback (guaranteed cross-serverless persistence)
            if (status.isBlank() || status == "PENDING") {
                try {
                    val encodedUser = java.net.URLEncoder.encode(cleanUser, "UTF-8")
                    val kvUrl = java.net.URL("https://kvdb.io/6iH8JqG7j4Zk8P2vNwKx1y/$encodedUser?_t=${System.currentTimeMillis()}")
                    val kvConn = kvUrl.openConnection() as java.net.HttpURLConnection
                    kvConn.requestMethod = "GET"
                    kvConn.useCaches = false
                    kvConn.connectTimeout = 3000
                    kvConn.readTimeout = 3000

                    if (kvConn.responseCode == 200) {
                        val kvRaw = kvConn.inputStream.bufferedReader().use { it.readText() }
                        if (kvRaw.contains("APPROVED", ignoreCase = true)) {
                            status = "APPROVED"
                        } else if (kvRaw.contains("DENIED", ignoreCase = true)) {
                            status = "DENIED"
                        }
                    }
                    kvConn.disconnect()
                } catch (e: Exception) {
                    android.util.Log.w("GateViewModel", "KV check error: ${e.message}")
                }
            }

            // 4. Check local database status in case admin approved via Admin Dashboard
            val norm = com.example.security.SecurityUtils.normalizeUsername(cleanUser)
            val dbUser = db.appDao().getUserByNormalizedUsername(norm)

            if (dbUser != null && dbUser.status == "APPROVED" && status.isBlank()) {
                status = "APPROVED"
            }

            android.util.Log.d("GateViewModel", "Polling status for '$cleanUser': '$status'")

            if (status == "APPROVED" || status == "APPROVE") {
                val userToUnlock = dbUser ?: db.appDao().getUserByNormalizedUsername(norm)
                if (userToUnlock != null) {
                    repository.approveUser(userToUnlock.id)
                    var device = db.appDao().getDevice(userToUnlock.id, deviceManager.getCurrentDeviceIdentifier())
                    if (device != null) {
                        repository.approveDevice(device.id)
                    } else {
                        val profile = deviceManager.hardwareProfile
                        val newDev = com.example.data.DeviceEntity(
                            userId = userToUnlock.id,
                            secureDeviceIdentifier = profile.identifier,
                            deviceType = profile.deviceType,
                            operatingSystem = profile.os,
                            ipAddress = profile.ip,
                            approximateRegion = "${profile.region}, ${profile.country}",
                            approvalStatus = "APPROVED"
                        )
                        val newDevId = db.appDao().insertDevice(newDev)
                        device = db.appDao().getDeviceById(newDevId)
                    }

                    // Generate active portal session directly so user enters immediately
                    val sessionToken = com.example.security.SecurityUtils.generateSecureToken(32)
                    val session = com.example.data.SessionEntity(
                        userId = userToUnlock.id,
                        deviceId = device?.id ?: 1L,
                        tokenHash = sessionToken,
                        expiresAt = System.currentTimeMillis() + java.util.concurrent.TimeUnit.HOURS.toMillis(12)
                    )
                    db.appDao().insertSession(session)
                    db.appDao().updateLastLogin(userToUnlock.id)

                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        _authenticatedUser.value = userToUnlock
                        _sessionToken.value = sessionToken
                        _authUiState.value = _authUiState.value.copy(
                            isLoading = false,
                            errorMessage = null,
                            successMessage = "Access approved by Administrator! Welcome, ${userToUnlock.username}!"
                        )
                        _currentScreen.value = AppScreen.DESTINATION
                    }
                }
            } else if (status == "DENIED" || status == "DENY") {
                if (dbUser != null) {
                    repository.denyUser(dbUser.id)
                }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _currentScreen.value = AppScreen.USER_DENIED
                }
            }
        }
    }
}
