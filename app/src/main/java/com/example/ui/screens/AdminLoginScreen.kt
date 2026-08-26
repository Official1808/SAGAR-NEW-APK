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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.GateViewModel
import com.example.ui.components.GatePrimaryButton
import com.example.ui.components.GateSecondaryButton
import com.example.ui.components.GateTextField
import com.example.ui.components.MessageBanner
import com.example.ui.theme.GateBlack
import com.example.ui.theme.GateBorder
import com.example.ui.theme.GateBorderRed
import com.example.ui.theme.GateCard
import com.example.ui.theme.GateDarkGray
import com.example.ui.theme.GateRedDark
import com.example.ui.theme.GateRedLight
import com.example.ui.theme.GateRedPrimary
import com.example.ui.theme.GateTextMuted
import com.example.ui.theme.GateTextSecondary
import com.example.ui.theme.GateWhite

@Composable
fun AdminLoginScreen(
    viewModel: GateViewModel,
    modifier: Modifier = Modifier
) {
    val authUiState by viewModel.authUiState.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
        Spacer(modifier = Modifier.height(28.dp))

        // Admin Shield Emblem
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GateRedDark, GateDarkGray)
                    )
                )
                .border(2.dp, GateRedPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = "Admin Security Control",
                tint = GateRedLight,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ADMIN PORTAL",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                fontSize = 24.sp
            ),
            color = GateWhite
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "SECURITY ACCESS CONTROL & OVERSIGHT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontSize = 10.sp
            ),
            color = GateRedLight
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Message banner
        if (authUiState.errorMessage != null) {
            MessageBanner(
                message = authUiState.errorMessage,
                isError = true,
                onDismiss = { viewModel.clearMessages() }
            )
        }
        if (authUiState.successMessage != null) {
            MessageBanner(
                message = authUiState.successMessage,
                isError = false,
                onDismiss = { viewModel.clearMessages() }
            )
        }

        // Admin Auth Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, GateBorderRed, RoundedCornerShape(16.dp)),
            color = GateCard
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                GateTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Admin Username",
                    placeholder = "Enter administrator username",
                    leadingIcon = Icons.Default.Person,
                    testTag = "admin_username_input"
                )

                Spacer(modifier = Modifier.height(14.dp))

                GateTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Admin Password",
                    placeholder = "••••••••",
                    leadingIcon = Icons.Default.Key,
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
                    testTag = "admin_password_input"
                )

                Spacer(modifier = Modifier.height(20.dp))

                GatePrimaryButton(
                    text = "AUTHENTICATE AS ADMIN",
                    onClick = {
                        viewModel.adminLogin(username, password)
                    },
                    isLoading = authUiState.isLoading,
                    icon = Icons.Default.Lock,
                    testTag = "admin_login_submit_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        GateSecondaryButton(
            text = "RETURN TO USER LOGIN",
            onClick = {
                viewModel.navigateTo(AppScreen.USER_AUTH)
            },
            icon = Icons.Default.ArrowBack,
            testTag = "back_to_user_auth_button"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
