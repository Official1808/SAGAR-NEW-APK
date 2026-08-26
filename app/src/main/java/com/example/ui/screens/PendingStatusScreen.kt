package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.GateViewModel
import com.example.ui.components.GatePrimaryButton
import com.example.ui.components.GateSecondaryButton
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
import com.example.ui.theme.GateRedDark
import com.example.ui.theme.GateRedLight
import com.example.ui.theme.GateRedPrimary
import com.example.ui.theme.GateTextMuted
import com.example.ui.theme.GateTextSecondary
import com.example.ui.theme.GateWhite

@Composable
fun PendingStatusScreen(
    viewModel: GateViewModel,
    screenType: AppScreen,
    modifier: Modifier = Modifier
) {
    val authUiState by viewModel.authUiState.collectAsState()
    val activeProfile by viewModel.activeDeviceProfile.collectAsState()

    val (title, description, iconVector, badgeColor, statusTag) = when (screenType) {
        AppScreen.USER_PENDING -> {
            Tuple5(
                "APPROVAL PENDING",
                "Your account is waiting for administrator approval.",
                Icons.Default.HourglassEmpty,
                GateAmber,
                "PENDING"
            )
        }
        AppScreen.DEVICE_PENDING -> {
            Tuple5(
                "DEVICE NOT APPROVED",
                "This device is not approved yet. A request has been sent to the administrator.",
                Icons.Default.LockClock,
                GateAmber,
                "PENDING DEVICE"
            )
        }
        AppScreen.USER_DENIED -> {
            Tuple5(
                "ACCESS DENIED",
                "Your access request was denied by the administrator.",
                Icons.Default.Warning,
                GateRedPrimary,
                "DENIED"
            )
        }
        AppScreen.DEVICE_DENIED -> {
            Tuple5(
                "DEVICE DENIED",
                "Access for this device was denied or revoked by the administrator.",
                Icons.Default.Warning,
                GateRedPrimary,
                "DENIED DEVICE"
            )
        }
        AppScreen.USER_BLOCKED -> {
            Tuple5(
                "ACCOUNT BLOCKED",
                "Your account has been blocked by the administrator. Contact support.",
                Icons.Default.Block,
                GateRedPrimary,
                "BLOCKED"
            )
        }
        else -> Tuple5("SECURITY GATEWAY", "", Icons.Default.Warning, GateRedPrimary, "STATUS")
    }

    val scrollState = rememberScrollState()

    // Periodically poll Vercel / database every 2 seconds when in pending state
    androidx.compose.runtime.LaunchedEffect(authUiState.pendingUsername, screenType) {
        val username = authUiState.pendingUsername
        if (!username.isNullOrBlank() && (screenType == AppScreen.USER_PENDING || screenType == AppScreen.DEVICE_PENDING)) {
            while (true) {
                kotlinx.coroutines.delay(2000)
                viewModel.checkVercelStatus(username)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GateBlack)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // Status Animated Icon Disc
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(badgeColor.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
                .border(2.dp, badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        StatusBadge(status = statusTag)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            ),
            color = GateWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = GateTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Access Request Details Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateBorder, RoundedCornerShape(16.dp)),
            color = GateCardElevated,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "HARDWARE & SECURITY PARAMETERS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GateRedLight,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                authUiState.pendingUsername?.let { u ->
                    DetailItem(label = "Target Username", value = u)
                }

                DetailItem(label = "Hardware Device", value = activeProfile.deviceType)
                DetailItem(label = "Operating System", value = activeProfile.os)
                DetailItem(label = "Hardware Identifier", value = activeProfile.identifier, isMono = true)
                DetailItem(label = "Network IP", value = activeProfile.ip, isMono = true)
                DetailItem(label = "Location", value = "${activeProfile.region}, ${activeProfile.country}")
                DetailItem(label = "Security Protocol", value = "TLS Encrypted / Device-Locked")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Real-time sync indicator
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateBorder, RoundedCornerShape(12.dp)),
            color = GateCardElevated,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = GateAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Live Sync Active: Polling for email click...",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = GateAmber
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        GatePrimaryButton(
            text = "CHECK STATUS NOW",
            onClick = {
                viewModel.checkVercelStatus(authUiState.pendingUsername)
            },
            icon = Icons.Default.Refresh,
            testTag = "check_status_now_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        GateSecondaryButton(
            text = "BACK TO LOGIN",
            onClick = {
                viewModel.navigateTo(AppScreen.USER_AUTH)
            },
            icon = Icons.Default.ArrowBack,
            testTag = "back_to_login_button"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    isMono: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = GateTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default
            ),
            color = GateWhite
        )
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
