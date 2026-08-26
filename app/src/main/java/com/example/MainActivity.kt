package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.GateViewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.DestinationScreen
import com.example.ui.screens.PendingStatusScreen
import com.example.ui.screens.UserAuthScreen
import com.example.ui.theme.GateBlack
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)
        setContent {
            MyApplicationTheme {
                PWSaraApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null) {
            val token = data.getQueryParameter("token") ?: data.lastPathSegment
            if (!token.isNullOrBlank() && (token.startsWith("APP_") || token.startsWith("DEN_"))) {
                viewModel.processEmailToken(token)
                Toast.makeText(this, "Processing Email Token Action...", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun PWSaraApp(
    viewModel: GateViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = GateBlack
    ) { innerPadding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentScreen) {
            AppScreen.USER_AUTH -> {
                UserAuthScreen(
                    viewModel = viewModel,
                    modifier = modifier
                )
            }
            AppScreen.USER_PENDING -> {
                PendingStatusScreen(
                    viewModel = viewModel,
                    screenType = AppScreen.USER_PENDING,
                    modifier = modifier
                )
            }
            AppScreen.DEVICE_PENDING -> {
                PendingStatusScreen(
                    viewModel = viewModel,
                    screenType = AppScreen.DEVICE_PENDING,
                    modifier = modifier
                )
            }
            AppScreen.USER_DENIED -> {
                PendingStatusScreen(
                    viewModel = viewModel,
                    screenType = AppScreen.USER_DENIED,
                    modifier = modifier
                )
            }
            AppScreen.DEVICE_DENIED -> {
                PendingStatusScreen(
                    viewModel = viewModel,
                    screenType = AppScreen.DEVICE_DENIED,
                    modifier = modifier
                )
            }
            AppScreen.USER_BLOCKED -> {
                PendingStatusScreen(
                    viewModel = viewModel,
                    screenType = AppScreen.USER_BLOCKED,
                    modifier = modifier
                )
            }
            AppScreen.DESTINATION -> {
                DestinationScreen(
                    viewModel = viewModel,
                    modifier = modifier
                )
            }
            AppScreen.ADMIN_LOGIN -> {
                AdminLoginScreen(
                    viewModel = viewModel,
                    modifier = modifier
                )
            }
            AppScreen.ADMIN_DASHBOARD -> {
                AdminDashboardScreen(
                    viewModel = viewModel,
                    modifier = modifier
                )
            }
        }
    }
}
