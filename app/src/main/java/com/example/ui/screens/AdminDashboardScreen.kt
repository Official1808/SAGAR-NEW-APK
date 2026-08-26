package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccessRequestEntity
import com.example.data.AuditLogEntity
import com.example.data.DeviceEntity
import com.example.data.EmailNotificationEntity
import com.example.data.UserEntity
import com.example.data.UserLinkEntity
import com.example.ui.AdminTab
import com.example.ui.GateViewModel
import com.example.ui.components.GatePrimaryButton
import com.example.ui.components.GateSecondaryButton
import com.example.ui.components.GateTextField
import com.example.ui.components.MessageBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GateAmber
import com.example.ui.theme.GateAmberDark
import com.example.ui.theme.GateBlack
import com.example.ui.theme.GateBorder
import com.example.ui.theme.GateBorderRed
import com.example.ui.theme.GateCard
import com.example.ui.theme.GateCardElevated
import com.example.ui.theme.GateDarkGray
import com.example.ui.theme.GateGreen
import com.example.ui.theme.GateGreenDark
import com.example.ui.theme.GateRedDark
import com.example.ui.theme.GateRedLight
import com.example.ui.theme.GateRedPrimary
import com.example.ui.theme.GateTextMuted
import com.example.ui.theme.GateTextSecondary
import com.example.ui.theme.GateWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: GateViewModel,
    modifier: Modifier = Modifier
) {
    val adminTab by viewModel.adminTab.collectAsState()
    val authUiState by viewModel.authUiState.collectAsState()

    val users by viewModel.users.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val allRequests by viewModel.allAccessRequests.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val emailNotifications by viewModel.emailNotifications.collectAsState()
    val selectedUserForHistory by viewModel.selectedUserForHistory.collectAsState()
    val allUserLinks by viewModel.allUserLinks.collectAsState()
    val selectedUserLinks by viewModel.selectedUserLinks.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GateBlack)
    ) {
        // Top Admin Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateBorderRed),
            color = GateDarkGray
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GateRedPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = GateWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PW SARA ADMIN",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = GateWhite
                        )
                        Text(
                            text = "Security Controller (Admin)",
                            style = MaterialTheme.typography.labelSmall,
                            color = GateRedLight
                        )
                    }
                }

                // Sign Out Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, GateBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.adminLogout() }
                        .testTag("admin_logout_button"),
                    color = GateCard
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = GateTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LOGOUT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GateWhite
                        )
                    }
                }
            }
        }

        // Horizontal Scrollable Tab Bar
        val tabScrollState = rememberScrollState()
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GateCard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(tabScrollState)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                AdminTabButton(
                    title = "Overview",
                    icon = Icons.Default.Shield,
                    isSelected = adminTab == AdminTab.OVERVIEW,
                    onClick = { viewModel.setAdminTab(AdminTab.OVERVIEW) }
                )
                AdminTabButton(
                    title = "Approvals (${pendingRequests.size})",
                    icon = Icons.Default.HourglassEmpty,
                    isSelected = adminTab == AdminTab.PENDING_APPROVALS,
                    badgeCount = pendingRequests.size,
                    onClick = { viewModel.setAdminTab(AdminTab.PENDING_APPROVALS) }
                )
                AdminTabButton(
                    title = "Users (${users.filter { it.role != "ADMIN" }.size})",
                    icon = Icons.Default.People,
                    isSelected = adminTab == AdminTab.USERS,
                    onClick = { viewModel.setAdminTab(AdminTab.USERS) }
                )
                AdminTabButton(
                    title = "User History",
                    icon = Icons.Default.History,
                    isSelected = adminTab == AdminTab.USER_HISTORY,
                    onClick = { viewModel.setAdminTab(AdminTab.USER_HISTORY) }
                )
                AdminTabButton(
                    title = "Devices (${devices.size})",
                    icon = Icons.Default.Devices,
                    isSelected = adminTab == AdminTab.DEVICES,
                    onClick = { viewModel.setAdminTab(AdminTab.DEVICES) }
                )
                AdminTabButton(
                    title = "Email Alerts (${emailNotifications.size})",
                    icon = Icons.Default.Email,
                    isSelected = adminTab == AdminTab.EMAIL_ALERTS,
                    onClick = { viewModel.setAdminTab(AdminTab.EMAIL_ALERTS) }
                )
                AdminTabButton(
                    title = "Audit Logs",
                    icon = Icons.Default.Security,
                    isSelected = adminTab == AdminTab.AUDIT_LOGS,
                    onClick = { viewModel.setAdminTab(AdminTab.AUDIT_LOGS) }
                )
                AdminTabButton(
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = adminTab == AdminTab.SETTINGS,
                    onClick = { viewModel.setAdminTab(AdminTab.SETTINGS) }
                )
            }
        }

        // Global Alert Banners inside Admin Dashboard
        authUiState.errorMessage?.let { error ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                MessageBanner(message = error, isError = true, onDismiss = { viewModel.clearMessages() })
            }
        }

        authUiState.successMessage?.let { success ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                MessageBanner(message = success, isError = false, onDismiss = { viewModel.clearMessages() })
            }
        }

        // Active Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (adminTab) {
                AdminTab.OVERVIEW -> OverviewTab(
                    users = users,
                    devices = devices,
                    pendingRequests = pendingRequests,
                    emailNotifications = emailNotifications,
                    auditLogs = auditLogs,
                    onApproveRequest = { viewModel.processEmailToken(it.approvalToken) },
                    onDenyRequest = { viewModel.processEmailToken(it.denyToken) },
                    onResendEmail = { viewModel.resendApprovalEmail(it.id) },
                    onNavigateTab = { viewModel.setAdminTab(it) },
                    onSelectUserHistory = { viewModel.selectUserForHistory(it) }
                )
                AdminTab.PENDING_APPROVALS -> PendingApprovalsTab(
                    pendingRequests = pendingRequests,
                    adminEmail = viewModel.emailService.adminEmail,
                    isSmtpConfigured = viewModel.emailService.isConfigured(),
                    onApprove = { viewModel.processEmailToken(it.approvalToken) },
                    onDeny = { viewModel.processEmailToken(it.denyToken) },
                    onResendEmail = { viewModel.resendApprovalEmail(it.id) },
                    onNavigateToSettings = { viewModel.setAdminTab(AdminTab.SETTINGS) },
                    onTestEmail = { viewModel.sendTestVerificationEmail() }
                )
                AdminTab.USERS -> UsersTab(
                    users = users,
                    allUserLinks = allUserLinks,
                    allDevices = devices,
                    onApproveUser = { viewModel.approveUser(it) },
                    onDenyUser = { viewModel.denyUser(it) },
                    onBlockUser = { viewModel.blockUser(it) },
                    onUnblockUser = { viewModel.unblockUser(it) },
                    onViewHistory = { user -> viewModel.selectUserForHistory(user) }
                )
                AdminTab.USER_HISTORY -> UserHistoryTab(
                    allUsers = users.filter { it.role != "ADMIN" },
                    selectedUser = selectedUserForHistory,
                    allAuditLogs = auditLogs,
                    allDevices = devices,
                    allUserLinks = allUserLinks,
                    onSelectUser = { viewModel.selectUserForHistory(it) },
                    onAddLink = { userId, username, title, url, desc ->
                        viewModel.addUserLink(userId, username, title, url, desc)
                    },
                    onDeleteLink = { linkId ->
                        viewModel.deleteUserLink(linkId)
                    },
                    onApproveUser = { viewModel.approveUser(it) },
                    onDenyUser = { viewModel.denyUser(it) },
                    onBlockUser = { viewModel.blockUser(it) },
                    onUnblockUser = { viewModel.unblockUser(it) }
                )
                AdminTab.DEVICES -> DevicesTab(
                    devices = devices,
                    users = users,
                    onApproveDevice = { viewModel.approveDevice(it) },
                    onDenyDevice = { viewModel.denyDevice(it) },
                    onRevokeDevice = { viewModel.revokeDevice(it) },
                    onRemoveDevice = { viewModel.removeDevice(it) }
                )
                AdminTab.EMAIL_ALERTS -> EmailAlertsTab(
                    emailNotifications = emailNotifications,
                    recipientEmail = viewModel.emailService.recipientEmail,
                    smtpSenderEmail = viewModel.emailService.smtpSenderEmail,
                    isSmtpConfigured = viewModel.emailService.isConfigured(),
                    onProcessToken = { viewModel.processEmailToken(it) },
                    onResendRequestEmail = { requestId -> viewModel.resendApprovalEmail(requestId) },
                    onNavigateToSettings = { viewModel.setAdminTab(AdminTab.SETTINGS) },
                    onTestEmail = { viewModel.sendTestVerificationEmail() }
                )
                AdminTab.AUDIT_LOGS -> AuditLogsTab(auditLogs = auditLogs)
                AdminTab.SETTINGS -> SettingsTab(
                    viewModel = viewModel
                )
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: OVERVIEW
// ----------------------------------------------------
@Composable
private fun OverviewTab(
    users: List<UserEntity>,
    devices: List<DeviceEntity>,
    pendingRequests: List<AccessRequestEntity>,
    emailNotifications: List<EmailNotificationEntity>,
    auditLogs: List<AuditLogEntity>,
    onApproveRequest: (AccessRequestEntity) -> Unit,
    onDenyRequest: (AccessRequestEntity) -> Unit,
    onResendEmail: (AccessRequestEntity) -> Unit,
    onNavigateTab: (AdminTab) -> Unit,
    onSelectUserHistory: (UserEntity) -> Unit
) {
    val scrollState = rememberScrollState()
    val studentUsers = users.filter { it.role != "ADMIN" }
    val approvedUsers = studentUsers.filter { it.status == "APPROVED" }
    val approvedDevices = devices.filter { it.approvalStatus == "APPROVED" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Quick Stat Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "PENDING",
                value = "${pendingRequests.size}",
                icon = Icons.Default.HourglassEmpty,
                color = if (pendingRequests.isNotEmpty()) GateAmber else GateTextMuted,
                onClick = { onNavigateTab(AdminTab.PENDING_APPROVALS) }
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "STUDENTS",
                value = "${studentUsers.size}",
                icon = Icons.Default.People,
                color = GateGreen,
                onClick = { onNavigateTab(AdminTab.USERS) }
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "DEVICES",
                value = "${approvedDevices.size}",
                icon = Icons.Default.PhoneAndroid,
                color = GateRedLight,
                onClick = { onNavigateTab(AdminTab.DEVICES) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pending Approvals Action Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (pendingRequests.isNotEmpty()) GateBorderRed else GateBorder, RoundedCornerShape(14.dp)),
            color = GateCard,
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (pendingRequests.isNotEmpty()) GateAmber else GateTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PENDING AUTHORIZATION QUEUE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = GateWhite
                            )
                        )
                    }

                    if (pendingRequests.isNotEmpty()) {
                        StatusBadge(status = "${pendingRequests.size} PENDING")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (pendingRequests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GateGreen,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All requests processed. Zero pending queues.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GateTextSecondary
                            )
                        }
                    }
                } else {
                    pendingRequests.take(4).forEach { req ->
                        AccessRequestItem(
                            request = req,
                            onApprove = { onApproveRequest(req) },
                            onDeny = { onDenyRequest(req) },
                            onResendEmail = { onResendEmail(req) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent User Activity & Login History Snapshot
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateBorder, RoundedCornerShape(14.dp)),
            color = GateCard,
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = GateRedLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECENT USER LOGINS & AUDIT EVENTS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = GateWhite
                            )
                        )
                    }

                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GateRedLight,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable { onNavigateTab(AdminTab.AUDIT_LOGS) }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                auditLogs.take(5).forEach { log ->
                    AuditLogRowItem(log = log)
                    HorizontalDivider(color = GateBorder, thickness = 0.5.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ----------------------------------------------------
// TAB 2: PENDING APPROVALS
// ----------------------------------------------------
@Composable
private fun PendingApprovalsTab(
    pendingRequests: List<AccessRequestEntity>,
    adminEmail: String,
    isSmtpConfigured: Boolean,
    onApprove: (AccessRequestEntity) -> Unit,
    onDeny: (AccessRequestEntity) -> Unit,
    onResendEmail: (AccessRequestEntity) -> Unit,
    onNavigateToSettings: () -> Unit,
    onTestEmail: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Email Dispatch Status & Instruction Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isSmtpConfigured) GateGreenDark else GateBorderRed, RoundedCornerShape(12.dp)),
                color = GateDarkGray,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (isSmtpConfigured) GateGreen else GateAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EMAIL ALERT DISPATCH STATUS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GateWhite
                                )
                            )
                        }

                        StatusBadge(status = if (isSmtpConfigured) "SMTP DIRECT ACTIVE" else "HTTP FALLBACK")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Alerts are sent to: $adminEmail",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = GateWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!isSmtpConfigured) {
                        Text(
                            text = "💡 For guaranteed instant delivery to your Gmail inbox (no delay), enter your 16-character Google App Password in Settings.",
                            style = MaterialTheme.typography.labelSmall,
                            color = GateAmber
                        )
                    } else {
                        Text(
                            text = "✔ Google SMTP is active. Real-time emails are dispatched directly to your inbox.",
                            style = MaterialTheme.typography.labelSmall,
                            color = GateGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTestEmail,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GateWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GateGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = GateGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TEST EMAIL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }

                        if (!isSmtpConfigured) {
                            Button(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GateRedPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SETUP APP PASS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        if (pendingRequests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GateGreen,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Pending Access Requests",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GateWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "When new students register or login from unapproved devices, their requests and hardware security parameters appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GateTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "ACTIVE PENDING REQUESTS (${pendingRequests.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GateAmber,
                        letterSpacing = 1.sp
                    )
                )
            }
            items(pendingRequests, key = { it.id }) { req ->
                AccessRequestItem(
                    request = req,
                    onApprove = { onApprove(req) },
                    onDeny = { onDeny(req) },
                    onResendEmail = { onResendEmail(req) }
                )
            }
        }
    }
}

// ----------------------------------------------------
// TAB 3: USERS LIST & MANAGEMENT
// ----------------------------------------------------
@Composable
private fun UsersTab(
    users: List<UserEntity>,
    allUserLinks: List<UserLinkEntity>,
    allDevices: List<DeviceEntity>,
    onApproveUser: (Long) -> Unit,
    onDenyUser: (Long) -> Unit,
    onBlockUser: (Long) -> Unit,
    onUnblockUser: (Long) -> Unit,
    onViewHistory: (UserEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val studentUsers = users.filter { it.role != "ADMIN" }
    val filteredUsers = studentUsers.filter {
        it.username.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("user_search_input"),
            placeholder = { Text("Search students by username...", color = GateTextMuted) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = GateTextSecondary)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GateDarkGray,
                unfocusedContainerColor = GateDarkGray,
                focusedTextColor = GateWhite,
                unfocusedTextColor = GateWhite,
                focusedBorderColor = GateRedPrimary,
                unfocusedBorderColor = GateBorder
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No users matching '$searchQuery'" else "No students registered yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GateTextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUsers, key = { it.id }) { user ->
                    val userLinksCount = allUserLinks.count { it.userId == user.id }
                    val userDevicesCount = allDevices.count { it.userId == user.id && it.approvalStatus == "APPROVED" }

                    UserManagementCard(
                        user = user,
                        linksCount = userLinksCount,
                        approvedDevicesCount = userDevicesCount,
                        onApprove = { onApproveUser(user.id) },
                        onDeny = { onDenyUser(user.id) },
                        onBlock = { onBlockUser(user.id) },
                        onUnblock = { onUnblockUser(user.id) },
                        onManageLinksAndHistory = { onViewHistory(user) }
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 4: USER FULL LOGIN & ACTIVITY HISTORY + ASSIGNED LINKS
// ----------------------------------------------------
@Composable
private fun UserHistoryTab(
    allUsers: List<UserEntity>,
    selectedUser: UserEntity?,
    allAuditLogs: List<AuditLogEntity>,
    allDevices: List<DeviceEntity>,
    allUserLinks: List<UserLinkEntity>,
    onSelectUser: (UserEntity?) -> Unit,
    onAddLink: (userId: Long, username: String, title: String, url: String, description: String) -> Unit,
    onDeleteLink: (Long) -> Unit,
    onApproveUser: (Long) -> Unit,
    onDenyUser: (Long) -> Unit,
    onBlockUser: (Long) -> Unit,
    onUnblockUser: (Long) -> Unit
) {
    val context = LocalContext.current
    var userDropdownExpanded by remember { mutableStateOf(false) }

    // State for Add Link Form
    var isAddLinkFormVisible by remember { mutableStateOf(false) }
    var newLinkTitle by remember { mutableStateOf("") }
    var newLinkUrl by remember { mutableStateOf("") }
    var newLinkDescription by remember { mutableStateOf("") }

    val activeUser = selectedUser ?: allUsers.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // User Selector Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateBorderRed, RoundedCornerShape(12.dp)),
            color = GateDarkGray,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SELECT STUDENT TO MANAGE CUSTOM LINKS & VIEW FULL HISTORY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GateRedLight,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GateCard)
                        .clickable { userDropdownExpanded = !userDropdownExpanded }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = GateWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeUser?.username ?: "No student selected",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = GateWhite
                        )
                    }

                    Text(
                        text = if (userDropdownExpanded) "▲ Close List" else "▼ Choose Student (${allUsers.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GateRedLight,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Dropdown of users
                if (userDropdownExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GateBlack)
                            .border(1.dp, GateBorder, RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        allUsers.forEach { u ->
                            val userLinkCount = allUserLinks.count { it.userId == u.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        onSelectUser(u)
                                        userDropdownExpanded = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = u.username,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (u.id == activeUser?.id) GateRedLight else GateWhite
                                    )
                                    Text(
                                        text = "$userLinkCount Links • ${if (u.lastLogin != null) "Last: " + formatTimestamp(u.lastLogin) else "No login yet"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GateTextMuted
                                    )
                                }
                                StatusBadge(status = u.status)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No students registered in database.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GateTextMuted
                )
            }
        } else {
            val userLogs = allAuditLogs.filter {
                it.userId == activeUser.id ||
                it.username.equals(activeUser.username, ignoreCase = true) ||
                it.details.contains(activeUser.username, ignoreCase = true)
            }
            val userDevices = allDevices.filter { it.userId == activeUser.id }
            val userAssignedLinks = allUserLinks.filter { it.userId == activeUser.id }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: User Summary & Account Control Card
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GateBorder, RoundedCornerShape(12.dp)),
                        color = GateCard,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = activeUser.username,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GateWhite
                                    )
                                    Text(
                                        text = "Registered: ${formatTimestamp(activeUser.createdAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GateTextMuted
                                    )
                                    Text(
                                        text = "Last Login Time: ${if (activeUser.lastLogin != null) formatTimestamp(activeUser.lastLogin) else "Never logged in"}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (activeUser.lastLogin != null) GateGreen else GateAmber
                                    )
                                }
                                StatusBadge(status = activeUser.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = GateBorder, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Registered Devices: ${userDevices.size} (${userDevices.count { it.approvalStatus == "APPROVED" }} Approved) • Assigned Links: ${userAssignedLinks.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GateTextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick Account Control Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (activeUser.status == "PENDING" || activeUser.status == "DENIED") {
                                    Button(
                                        onClick = { onApproveUser(activeUser.id) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = GateGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("APPROVE USER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }

                                if (activeUser.status == "APPROVED") {
                                    Button(
                                        onClick = { onBlockUser(activeUser.id) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = GateRedDark),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("BLOCK USER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }

                                if (activeUser.status == "BLOCKED") {
                                    Button(
                                        onClick = { onUnblockUser(activeUser.id) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = GateAmber),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("UNBLOCK USER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GateBlack))
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Manage Links For This User (Add Unlimited Links)
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GateBorderRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                        color = GateCardElevated,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = GateRedLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ASSIGNED STUDY LINKS FOR ${activeUser.username.uppercase()}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GateWhite
                                        )
                                    )
                                }

                                Button(
                                    onClick = { isAddLinkFormVisible = !isAddLinkFormVisible },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAddLinkFormVisible) GateDarkGray else GateRedPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isAddLinkFormVisible) Icons.Default.Close else Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isAddLinkFormVisible) "CANCEL" else "+ ADD LINK",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            // Add Link Expandable Form
                            if (isAddLinkFormVisible) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, GateBorderRed, RoundedCornerShape(10.dp)),
                                    color = GateBlack,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "ADD NEW STUDY RESOURCE (UNLIMITED ALLOWED)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = GateRedLight
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Link Title Input
                                        OutlinedTextField(
                                            value = newLinkTitle,
                                            onValueChange = { newLinkTitle = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("Link Title (e.g. Physics 2026 Batch Lectures)", color = GateTextMuted, fontSize = 12.sp) },
                                            label = { Text("Title for Student Display", color = GateTextSecondary, fontSize = 11.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = GateDarkGray,
                                                unfocusedContainerColor = GateDarkGray,
                                                focusedTextColor = GateWhite,
                                                unfocusedTextColor = GateWhite,
                                                focusedBorderColor = GateRedPrimary,
                                                unfocusedBorderColor = GateBorder
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Link URL Input
                                        OutlinedTextField(
                                            value = newLinkUrl,
                                            onValueChange = { newLinkUrl = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("Target URL (e.g. https://...)", color = GateTextMuted, fontSize = 12.sp) },
                                            label = { Text("Resource URL", color = GateTextSecondary, fontSize = 11.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = GateDarkGray,
                                                unfocusedContainerColor = GateDarkGray,
                                                focusedTextColor = GateWhite,
                                                unfocusedTextColor = GateWhite,
                                                focusedBorderColor = GateRedPrimary,
                                                unfocusedBorderColor = GateBorder
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Link Description Input
                                        OutlinedTextField(
                                            value = newLinkDescription,
                                            onValueChange = { newLinkDescription = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("Optional Notes (e.g. Batch 1 Chapter 4 DPP)", color = GateTextMuted, fontSize = 12.sp) },
                                            label = { Text("Description / Notes (Optional)", color = GateTextSecondary, fontSize = 11.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = GateDarkGray,
                                                unfocusedContainerColor = GateDarkGray,
                                                focusedTextColor = GateWhite,
                                                unfocusedTextColor = GateWhite,
                                                focusedBorderColor = GateRedPrimary,
                                                unfocusedBorderColor = GateBorder
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                if (newLinkTitle.isNotBlank() && newLinkUrl.isNotBlank()) {
                                                    var finalUrl = newLinkUrl.trim()
                                                    if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                                                        finalUrl = "https://$finalUrl"
                                                    }
                                                    onAddLink(
                                                        activeUser.id,
                                                        activeUser.username,
                                                        newLinkTitle.trim(),
                                                        finalUrl,
                                                        newLinkDescription.trim()
                                                    )
                                                    newLinkTitle = ""
                                                    newLinkUrl = ""
                                                    newLinkDescription = ""
                                                    isAddLinkFormVisible = false
                                                } else {
                                                    Toast.makeText(context, "Please enter both Title and URL", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = GateRedPrimary),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "SAVE AND ASSIGN LINK TO ${activeUser.username.uppercase()}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // List of Assigned Links for this User
                            if (userAssignedLinks.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No custom links added yet for ${activeUser.username}. Click '+ ADD LINK' above.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GateTextMuted
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    userAssignedLinks.forEach { link ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, GateBorder, RoundedCornerShape(8.dp)),
                                            color = GateBlack,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = link.title,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = GateWhite
                                                    )
                                                    Text(
                                                        text = link.url,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                                        color = GateRedLight,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (link.description.isNotBlank()) {
                                                        Text(
                                                            text = link.description,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = GateTextSecondary
                                                        )
                                                    }
                                                    Text(
                                                        text = "Added: ${formatTimestamp(link.createdAt)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = GateTextMuted
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Test Link
                                                    IconButton(
                                                        onClick = {
                                                            try {
                                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url)).apply {
                                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                                }
                                                                context.startActivity(intent)
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Cannot open URL: ${e.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.OpenInNew,
                                                            contentDescription = "Test Link",
                                                            tint = GateTextSecondary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }

                                                    // Delete Link
                                                    IconButton(
                                                        onClick = { onDeleteLink(link.id) },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Delete Link",
                                                            tint = GateRedLight,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: User Chronological History
                item {
                    Text(
                        text = "CHRONOLOGICAL LOGIN & ACTIVITY LOGS (${userLogs.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GateRedLight,
                            letterSpacing = 1.sp
                        )
                    )
                }

                if (userLogs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No activity logs recorded yet for ${activeUser.username}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GateTextMuted
                            )
                        }
                    }
                } else {
                    items(userLogs, key = { it.id }) { log ->
                        AuditLogRowItem(log = log)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 5: DEVICES MANAGEMENT
// ----------------------------------------------------
@Composable
private fun DevicesTab(
    devices: List<DeviceEntity>,
    users: List<UserEntity>,
    onApproveDevice: (Long) -> Unit,
    onDenyDevice: (Long) -> Unit,
    onRevokeDevice: (Long) -> Unit,
    onRemoveDevice: (Long) -> Unit
) {
    if (devices.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = GateTextMuted,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Registered Devices",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GateWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "When users attempt to log in, their original hardware IDs and specs are recorded here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GateTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "REGISTERED HARDWARE DEVICES (${devices.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GateRedLight,
                        letterSpacing = 1.sp
                    )
                )
            }

            items(devices, key = { it.id }) { dev ->
                val user = users.find { it.id == dev.userId }
                DeviceCard(
                    device = dev,
                    username = user?.username ?: "User #${dev.userId}",
                    onApprove = { onApproveDevice(dev.id) },
                    onDeny = { onDenyDevice(dev.id) },
                    onRevoke = { onRevokeDevice(dev.id) },
                    onRemove = { onRemoveDevice(dev.id) }
                )
            }
        }
    }
}

// ----------------------------------------------------
// TAB 6: EMAIL ALERTS
// ----------------------------------------------------
@Composable
private fun EmailAlertsTab(
    emailNotifications: List<EmailNotificationEntity>,
    recipientEmail: String,
    smtpSenderEmail: String,
    isSmtpConfigured: Boolean,
    onProcessToken: (String) -> Unit,
    onResendRequestEmail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onTestEmail: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Email Dispatch Status & Diagnostics Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isSmtpConfigured) GateGreenDark else GateBorderRed, RoundedCornerShape(12.dp)),
                color = GateDarkGray,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (isSmtpConfigured) GateGreen else GateAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EMAIL ALERT NOTIFICATIONS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GateWhite
                                )
                            )
                        }

                        StatusBadge(status = if (isSmtpConfigured) "SMTP DIRECT ACTIVE" else "HTTP FALLBACK")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Alerts Delivered To: $recipientEmail",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = GateWhite
                    )

                    Text(
                        text = "SMTP Sender: $smtpSenderEmail",
                        style = MaterialTheme.typography.labelSmall,
                        color = GateTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!isSmtpConfigured) {
                        Text(
                            text = "💡 For guaranteed instant inbox delivery, add your 16-character Google App Password for $smtpSenderEmail in Settings.",
                            style = MaterialTheme.typography.labelSmall,
                            color = GateAmber
                        )
                    } else {
                        Text(
                            text = "✔ Direct Google SMTP active. Instant authorization emails are dispatched via $smtpSenderEmail directly to $recipientEmail.",
                            style = MaterialTheme.typography.labelSmall,
                            color = GateGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTestEmail,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GateWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GateGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = GateGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TEST EMAIL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }

                        if (!isSmtpConfigured) {
                            Button(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GateRedPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SETUP APP PASS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        if (emailNotifications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = GateTextMuted,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Email Security Alerts Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GateWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All outgoing authorization emails to $recipientEmail will appear in this audit log.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GateTextSecondary
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "DISPATCHED SECURITY EMAILS (${emailNotifications.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GateRedLight,
                        letterSpacing = 1.sp
                    )
                )
            }

            items(emailNotifications, key = { it.id }) { email ->
                EmailCard(
                    email = email,
                    onProcessToken = onProcessToken,
                    onResend = { onResendRequestEmail(email.requestId) }
                )
            }
        }
    }
}

// ----------------------------------------------------
// TAB 7: AUDIT LOGS
// ----------------------------------------------------
@Composable
private fun AuditLogsTab(
    auditLogs: List<AuditLogEntity>
) {
    var searchFilter by remember { mutableStateOf("") }
    val filteredLogs = if (searchFilter.isBlank()) auditLogs else auditLogs.filter {
        (it.username?.contains(searchFilter, ignoreCase = true) == true) ||
        it.action.contains(searchFilter, ignoreCase = true) ||
        it.details.contains(searchFilter, ignoreCase = true) ||
        it.ipAddress.contains(searchFilter, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchFilter,
            onValueChange = { searchFilter = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Filter logs by user, action or IP...", color = GateTextMuted) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = GateTextSecondary)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GateDarkGray,
                unfocusedContainerColor = GateDarkGray,
                focusedTextColor = GateWhite,
                unfocusedTextColor = GateWhite,
                focusedBorderColor = GateRedPrimary,
                unfocusedBorderColor = GateBorder
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No audit logs found matching filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GateTextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    AuditLogRowItem(log = log)
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 8: SETTINGS (Gmail SMTP & Admin Security)
// ----------------------------------------------------
@Composable
private fun SettingsTab(
    viewModel: GateViewModel
) {
    val scrollState = rememberScrollState()

    var smtpSenderEmail by remember { mutableStateOf(viewModel.emailService.smtpSenderEmail) }
    var appPassword by remember { mutableStateOf(viewModel.emailService.appPassword) }
    var recipientEmail by remember { mutableStateOf(viewModel.emailService.recipientEmail) }
    var vercelBackendUrl by remember { mutableStateOf(viewModel.emailService.vercelBackendUrl) }
    var isAppPasswordVisible by remember { mutableStateOf(false) }

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmNewPass by remember { mutableStateOf("") }
    var isPassVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // GMAIL SMTP CONFIGURATION CARD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateBorderRed, RoundedCornerShape(16.dp)),
            color = GateCard,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = GateRedLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GMAIL SMTP DISPATCH ENGINE",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GateWhite
                            )
                        )
                    }

                    val isConfigured = viewModel.emailService.isConfigured()
                    StatusBadge(status = if (isConfigured) "CONFIGURED" else "PASSWORD NEEDED")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PW SARA sends instant authorization emails with Approve/Deny buttons to your designated Recipient Email using Google SMTP ($smtpSenderEmail) whenever students sign up or log in from new devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GateTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Alert Recipient Email Field (Where you want to receive emails)
                GateTextField(
                    value = recipientEmail,
                    onValueChange = { recipientEmail = it },
                    label = "Alert Recipient Email (Where YOU Receive Alerts)",
                    placeholder = "e.g. your_other_email@domain.com",
                    leadingIcon = Icons.Default.MarkEmailRead,
                    testTag = "recipient_email_input"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // SMTP Sender Gmail Address Field
                GateTextField(
                    value = smtpSenderEmail,
                    onValueChange = { smtpSenderEmail = it },
                    label = "SMTP Sender Gmail Address",
                    placeholder = "mesagarmeena@gmail.com",
                    leadingIcon = Icons.Default.Email,
                    testTag = "admin_email_input"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gmail App Password Field
                GateTextField(
                    value = appPassword,
                    onValueChange = { appPassword = it },
                    label = "Google App Password for $smtpSenderEmail (16 Letters)",
                    placeholder = "e.g. abcd efgh ijkl mnop",
                    leadingIcon = Icons.Default.Key,
                    trailingIcon = {
                        IconButton(onClick = { isAppPasswordVisible = !isAppPasswordVisible }) {
                            Icon(
                                imageVector = if (isAppPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle visibility",
                                tint = GateTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (isAppPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    testTag = "app_password_input"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Vercel Backend / Webhook URL Field
                GateTextField(
                    value = vercelBackendUrl,
                    onValueChange = { vercelBackendUrl = it },
                    label = "Vercel Backend URL (Approve / Deny Webhook)",
                    placeholder = "https://your-app.vercel.app",
                    leadingIcon = Icons.Default.Link,
                    testTag = "vercel_backend_url_input"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Step-by-step guidance
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GateBorder, RoundedCornerShape(10.dp)),
                    color = GateDarkGray,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "How it works:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GateWhite)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Sender Account: '$smtpSenderEmail' uses the 16-letter App Password to authenticate with Google SMTP.\n• Recipient Inbox: Emails are delivered directly to '$recipientEmail'.\n• Vercel Webhook: Email buttons will link directly to '$vercelBackendUrl' for 1-click web approvals in browser with ?action=approve|deny&token=...&username=...\n• 16-Letter App Password: Generate at myaccount.google.com/apppasswords under the '$smtpSenderEmail' Google Account.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                            color = GateTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Save Settings Button
                    Button(
                        onClick = {
                            viewModel.updateGmailSettings(
                                smtpSenderEmail = smtpSenderEmail,
                                appPassword = appPassword,
                                recipientEmail = recipientEmail,
                                vercelBackendUrl = vercelBackendUrl
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_email_settings_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GateRedPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE SETTINGS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    // Test Verification Email Button
                    OutlinedButton(
                        onClick = {
                            viewModel.sendTestVerificationEmail()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("send_test_email_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GateWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GateGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = GateGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SEND TEST", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GateGreen))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ADMIN CREDENTIALS UPDATE CARD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateBorder, RoundedCornerShape(16.dp)),
            color = GateCard,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = GateWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CHANGE ADMIN PASSWORD",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GateWhite
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                GateTextField(
                    value = currentPass,
                    onValueChange = { currentPass = it },
                    label = "Current Admin Password",
                    placeholder = "Enter current admin password",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = if (isPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    testTag = "current_admin_pass"
                )

                Spacer(modifier = Modifier.height(10.dp))

                GateTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = "New Admin Password",
                    placeholder = "••••••••",
                    leadingIcon = Icons.Default.Key,
                    visualTransformation = if (isPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    testTag = "new_admin_pass"
                )

                Spacer(modifier = Modifier.height(10.dp))

                GateTextField(
                    value = confirmNewPass,
                    onValueChange = { confirmNewPass = it },
                    label = "Confirm New Password",
                    placeholder = "••••••••",
                    leadingIcon = Icons.Default.Key,
                    visualTransformation = if (isPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    testTag = "confirm_new_admin_pass"
                )

                Spacer(modifier = Modifier.height(16.dp))

                GatePrimaryButton(
                    text = "UPDATE PASSWORD",
                    onClick = {
                        if (newPass != confirmNewPass) {
                            // Handled in VM
                        } else {
                            viewModel.changeAdminPassword(currentPass, newPass)
                            currentPass = ""
                            newPass = ""
                            confirmNewPass = ""
                        }
                    },
                    testTag = "submit_change_password_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ----------------------------------------------------
// REUSABLE COMPONENTS FOR ADMIN DASHBOARD
// ----------------------------------------------------

@Composable
private fun AdminTabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeCount: Int? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) GateRedPrimary else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) GateWhite else GateTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) GateWhite else GateTextSecondary
                )
            )
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, GateBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = GateCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GateTextMuted
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = color
                )
            )
        }
    }
}

@Composable
private fun AccessRequestItem(
    request: AccessRequestEntity,
    onApprove: () -> Unit,
    onDeny: () -> Unit,
    onResendEmail: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GateBorder, RoundedCornerShape(10.dp)),
        color = GateDarkGray,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (request.requestType == "SIGNUP") Icons.Default.Person else Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = GateRedLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = request.username,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GateWhite
                    )
                }

                StatusBadge(status = request.requestType)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Device: ${request.deviceType} (${request.operatingSystem})",
                style = MaterialTheme.typography.bodySmall,
                color = GateTextSecondary
            )
            Text(
                text = "IP: ${request.ipAddress} • ${request.approximateRegion}",
                style = MaterialTheme.typography.labelSmall,
                color = GateTextMuted
            )
            Text(
                text = "Time: ${formatTimestamp(request.createdAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = GateTextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Approve / Deny Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GateGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("APPROVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onDeny,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GateRedDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("DENY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Resend Email & Open Email Intent Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onResendEmail,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GateWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GateAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = GateAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RESEND EMAIL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GateAmber))
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val emailSubject = "[PW SARA AUTH] Security Request for ${request.username}"
                            val emailBody = """
                                PW SARA ACCESS REQUEST
                                Student: ${request.username}
                                Request Type: ${request.requestType}
                                Device: ${request.deviceType} (${request.operatingSystem})
                                IP Address: ${request.ipAddress} (${request.approximateRegion})
                                
                                APPROVAL LINK:
                                https://ais-pre-l64rjdrsqjec6nomagmwqg-611261864288.asia-southeast1.run.app/approve?token=${request.approvalToken}
                                
                                DENIAL LINK:
                                https://ais-pre-l64rjdrsqjec6nomagmwqg-611261864288.asia-southeast1.run.app/deny?token=${request.denyToken}
                            """.trimIndent()

                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:mesagarmeena@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                                putExtra(Intent.EXTRA_TEXT, emailBody)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No email client app found", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GateTextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GateBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = GateTextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("OPEN GMAIL", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun UserManagementCard(
    user: UserEntity,
    linksCount: Int,
    approvedDevicesCount: Int,
    onApprove: () -> Unit,
    onDeny: () -> Unit,
    onBlock: () -> Unit,
    onUnblock: () -> Unit,
    onManageLinksAndHistory: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GateBorder, RoundedCornerShape(12.dp)),
        color = GateCard,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GateWhite
                    )
                    Text(
                        text = "Registered: ${formatTimestamp(user.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GateTextMuted
                    )
                    Text(
                        text = "Last Login: ${if (user.lastLogin != null) formatTimestamp(user.lastLogin) else "Never logged in"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (user.lastLogin != null) GateGreen else GateTextSecondary
                    )
                }

                StatusBadge(status = user.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = GateBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Badges Row for Links & Devices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = GateBlack,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GateBorderRed.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, tint = GateRedLight, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$linksCount Assigned Links",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GateWhite)
                        )
                    }
                }

                Surface(
                    color = GateBlack,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GateBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Devices, contentDescription = null, tint = GateTextSecondary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$approvedDevicesCount Devices",
                            style = MaterialTheme.typography.labelSmall.copy(color = GateTextSecondary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Manage Links & History Button
                Button(
                    onClick = onManageLinksAndHistory,
                    modifier = Modifier.weight(1.4f),
                    colors = ButtonDefaults.buttonColors(containerColor = GateRedPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("MANAGE LINKS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                if (user.status == "PENDING" || user.status == "DENIED") {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GateGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("APPROVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                if (user.status == "APPROVED") {
                    Button(
                        onClick = onBlock,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GateRedDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("BLOCK", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                if (user.status == "BLOCKED") {
                    Button(
                        onClick = onUnblock,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GateAmber),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("UNBLOCK", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GateBlack))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: DeviceEntity,
    username: String,
    onApprove: () -> Unit,
    onDeny: () -> Unit,
    onRevoke: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GateBorder, RoundedCornerShape(12.dp)),
        color = GateCard,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = device.deviceType,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GateWhite
                    )
                    Text(
                        text = "Assigned to: $username",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = GateRedLight
                    )
                }

                StatusBadge(status = device.approvalStatus)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ID: ${device.secureDeviceIdentifier}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = GateTextMuted
            )
            Text(
                text = "OS: ${device.operatingSystem} • IP: ${device.ipAddress}",
                style = MaterialTheme.typography.labelSmall,
                color = GateTextSecondary
            )
            Text(
                text = "Location: ${device.approximateRegion}",
                style = MaterialTheme.typography.labelSmall,
                color = GateTextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (device.approvalStatus != "APPROVED") {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GateGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("APPROVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                } else {
                    Button(
                        onClick = onRevoke,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GateRedDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("REVOKE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                OutlinedButton(
                    onClick = onRemove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GateTextMuted),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GateBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("DELETE", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun EmailCard(
    email: EmailNotificationEntity,
    onProcessToken: (String) -> Unit,
    onResend: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GateBorder, RoundedCornerShape(12.dp)),
        color = GateCard,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = GateRedLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "To: ${email.recipientEmail}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = GateWhite
                    )
                }

                StatusBadge(status = email.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email.subject,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = GateWhite
            )
            Text(
                text = "Target: ${email.username} (${email.requestType}) • ${formatTimestamp(email.timestamp)}",
                style = MaterialTheme.typography.labelSmall,
                color = GateTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Token Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onProcessToken(email.approvalToken) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GateGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("EXECUTE APPROVAL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = { onProcessToken(email.denyToken) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GateRedDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("EXECUTE DENIAL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Resend & Open in Email App
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onResend,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GateAmber),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GateAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = GateAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RESEND EMAIL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${email.recipientEmail}")
                                putExtra(Intent.EXTRA_SUBJECT, email.subject)
                                putExtra(Intent.EXTRA_TEXT, "Approval Token: ${email.approvalToken}\nDenial Token: ${email.denyToken}\nTarget: ${email.username}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No email client app found", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GateTextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GateBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = GateTextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("OPEN IN EMAIL", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun AuditLogRowItem(log: AuditLogEntity) {
    val actionColor = when {
        log.action.contains("SUCCESS", ignoreCase = true) || log.action.contains("APPROVED", ignoreCase = true) -> GateGreen
        log.action.contains("FAILED", ignoreCase = true) || log.action.contains("DENIED", ignoreCase = true) || log.action.contains("BLOCKED", ignoreCase = true) -> GateRedPrimary
        log.action.contains("REQUEST", ignoreCase = true) || log.action.contains("PENDING", ignoreCase = true) -> GateAmber
        else -> GateTextSecondary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = actionColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = log.action,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = actionColor,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (!log.username.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.username,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = GateWhite
                    )
                }
            }

            Text(
                text = formatTimestamp(log.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = GateTextMuted
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = log.details,
            style = MaterialTheme.typography.bodySmall,
            color = GateTextSecondary
        )

        Text(
            text = "IP: ${log.ipAddress}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
            color = GateTextMuted
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
