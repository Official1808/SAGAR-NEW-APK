package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.GateViewModel
import com.example.ui.components.GatePrimaryButton
import com.example.ui.components.GateTextField
import com.example.ui.components.MessageBanner
import com.example.ui.components.PWGateHeader
import com.example.ui.theme.GateBlack
import com.example.ui.theme.GateBorder
import com.example.ui.theme.GateBorderRed
import com.example.ui.theme.GateCard
import com.example.ui.theme.GateDarkGray
import com.example.ui.theme.GateGreen
import com.example.ui.theme.GateRedLight
import com.example.ui.theme.GateRedPrimary
import com.example.ui.theme.GateTextMuted
import com.example.ui.theme.GateTextSecondary
import com.example.ui.theme.GateWhite

@Composable
fun UserAuthScreen(
    viewModel: GateViewModel,
    modifier: Modifier = Modifier
) {
    val authUiState by viewModel.authUiState.collectAsState()
    val activeProfile by viewModel.activeDeviceProfile.collectAsState()

    var isSignupMode by remember { mutableStateOf(false) }

    // Form fields
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GateBlack)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Bar (Admin Access Toggle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, GateBorder, RoundedCornerShape(8.dp))
                    .clickable { viewModel.navigateTo(AppScreen.ADMIN_LOGIN) }
                    .testTag("admin_portal_toggle_button"),
                color = GateCard
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Portal",
                        tint = GateRedLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ADMIN PORTAL",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GateTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cyber / Security Branding Header
        PWGateHeader(
            title = "PW SARA",
            subtitle = if (isSignupMode) "STUDENT ACCOUNT REGISTRATION" else "SECURE ACCESS GATEWAY"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Feedback Banners
        authUiState.errorMessage?.let { error ->
            MessageBanner(
                message = error,
                isError = true,
                onDismiss = { viewModel.clearMessages() }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        authUiState.successMessage?.let { success ->
            MessageBanner(
                message = success,
                isError = false,
                onDismiss = { viewModel.clearMessages() }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Tab Selector (LOGIN vs SIGN UP)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateBorder, RoundedCornerShape(12.dp)),
            color = GateDarkGray,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                // Login Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isSignupMode) GateRedPrimary else GateDarkGray)
                        .clickable {
                            isSignupMode = false
                            viewModel.clearMessages()
                        }
                        .padding(vertical = 12.dp)
                        .testTag("tab_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LOGIN",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (!isSignupMode) GateWhite else GateTextMuted
                        )
                    )
                }

                // Sign Up Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSignupMode) GateRedPrimary else GateDarkGray)
                        .clickable {
                            isSignupMode = true
                            viewModel.clearMessages()
                        }
                        .padding(vertical = 12.dp)
                        .testTag("tab_signup"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SIGN UP",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSignupMode) GateWhite else GateTextMuted
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Credentials Form Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateBorderRed, RoundedCornerShape(16.dp)),
            color = GateCard,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSignupMode) "CREATE NEW ACCOUNT" else "ENTER CREDENTIALS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = GateWhite
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Text(
                    text = if (isSignupMode)
                        "Enter your desired username & password. An approval request will be submitted to the administrator."
                    else
                        "Enter your credentials provided by the administrator to access the study portal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GateTextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp)
                )

                // Username field
                GateTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username",
                    placeholder = "Enter your username",
                    leadingIcon = Icons.Default.Person,
                    testTag = "username_input"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password field
                GateTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = if (isSignupMode) "Create Password" else "Password",
                    placeholder = "••••••••",
                    leadingIcon = Icons.Default.Lock,
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = GateTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    testTag = "password_input"
                )

                // Confirm Password field for Signup
                if (isSignupMode) {
                    Spacer(modifier = Modifier.height(14.dp))
                    GateTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        testTag = "confirm_password_input"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button
                if (isSignupMode) {
                    GatePrimaryButton(
                        text = "SIGN UP",
                        onClick = {
                            viewModel.userSignup(username, password, confirmPassword)
                        },
                        isLoading = authUiState.isLoading,
                        testTag = "submit_signup_button"
                    )
                } else {
                    GatePrimaryButton(
                        text = "LOGIN",
                        onClick = {
                            viewModel.userLogin(username, password)
                        },
                        isLoading = authUiState.isLoading,
                        testTag = "submit_login_button"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Authentic Hardware Device Binding Card (Real Device Details Only)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateBorder, RoundedCornerShape(14.dp)),
            color = GateDarkGray.copy(alpha = 0.6f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = GateRedLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ORIGINAL DEVICE HARDWARE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GateWhite,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    Surface(
                        color = GateGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GateGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = GateGreen,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "LOCKED HARDWARE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GateGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Model: ${activeProfile.deviceType}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = GateWhite
                        )
                        Text(
                            text = "OS: ${activeProfile.os}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GateTextSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = activeProfile.identifier,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GateRedLight
                        )
                        Text(
                            text = "Location: ${activeProfile.country}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GateTextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
