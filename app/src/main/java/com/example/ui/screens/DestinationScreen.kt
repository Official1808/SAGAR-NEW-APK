package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.GateRepository
import com.example.data.UserLinkEntity
import com.example.ui.GateViewModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DestinationScreen(
    viewModel: GateViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.authenticatedUser.collectAsState()
    val userLinks by viewModel.linksForCurrentUser.collectAsState()

    // Currently active resource link opened by user (null means viewing link list)
    var activeOpenedLink by remember { mutableStateOf<UserLinkEntity?>(null) }
    var activeFallbackUrl by remember { mutableStateOf<String?>(null) }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var webProgress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isControlMenuExpanded by remember { mutableStateOf(false) }

    // Intercept hardware/gesture back press
    BackHandler(enabled = activeOpenedLink != null || activeFallbackUrl != null || canGoBack) {
        if (canGoBack && webViewInstance != null) {
            webViewInstance?.goBack()
        } else {
            // Close webview and return to User Title List
            activeOpenedLink = null
            activeFallbackUrl = null
            webViewInstance = null
        }
    }

    // Helper function to handle PDF and file downloads - immediately opens in browser without waiting
    fun handlePdfOrDownload(url: String, userAgent: String?, contentDisposition: String?, mimeType: String?) {
        try {
            val filename = URLUtil.guessFileName(url, contentDisposition, mimeType)

            // 1. Immediately launch in system browser / PDF viewer without waiting
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (!mimeType.isNullOrBlank() && mimeType.contains("pdf", ignoreCase = true)) {
                        setDataAndType(Uri.parse(url), "application/pdf")
                    }
                }
                context.startActivity(browserIntent)
            } catch (e: Exception) {
                // If direct viewer failed, launch via generic browser intent or Google Docs viewer
                try {
                    val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(genericIntent)
                } catch (e2: Exception) {
                    try {
                        val encoded = java.net.URLEncoder.encode(url, "UTF-8")
                        val googleDocsViewer = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/viewer?url=$encoded")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(googleDocsViewer)
                    } catch (ignored: Exception) {}
                }
            }

            // 2. Also start background download simultaneously so file is preserved on device
            try {
                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(if (mimeType.isNullOrBlank() || mimeType == "application/octet-stream") "application/pdf" else mimeType)
                    addRequestHeader("User-Agent", userAgent ?: "")
                    addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url) ?: "")
                    setDescription("Downloading study material...")
                    setTitle(filename)
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                }

                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                downloadManager?.enqueue(request)
            } catch (downloadEx: Exception) {
                // Download manager optional if browser handles direct viewing
            }

            Toast.makeText(context, "Opening PDF in browser: $filename", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Opening file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val currentUrlToLoad = activeOpenedLink?.url ?: activeFallbackUrl

    if (currentUrlToLoad == null) {
        // --- SCREEN 1: USER TITLE DASHBOARD (Displays only titles as requested) ---
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(GateBlack)
        ) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = GateCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, GateBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GateRedDark.copy(alpha = 0.5f))
                                .border(1.dp, GateRedPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = GateRedLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Welcome, ${user?.username ?: "Student"}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = GateWhite
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = GateGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Device Authenticated & Secured",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = GateGreen
                                )
                            }
                        }
                    }

                    // Logout Button
                    OutlinedButtonWithIcon(
                        text = "LOGOUT",
                        icon = Icons.Default.PowerSettingsNew,
                        onClick = { viewModel.userLogout() }
                    )
                }
            }

            // Body Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Portal Title Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GateBorderRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        color = GateCard,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MY ASSIGNED COURSES & MATERIALS",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = GateRedLight
                                )

                                Surface(
                                    color = GateRedDark.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(20.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GateRedPrimary.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "${userLinks.size} Available",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GateWhite,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Select any assigned study title below to access your class lectures and study resources.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GateTextSecondary
                            )
                        }
                    }
                }

                if (userLinks.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GateBorder, RoundedCornerShape(14.dp)),
                            color = GateCardElevated,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = GateTextMuted,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Custom Links Assigned Yet",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GateWhite
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "The administrator has not added custom links for your account yet. You can still access the main study portal below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GateTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        activeFallbackUrl = GateRepository.DESTINATION_URL
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GateRedPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("OPEN MAIN STUDY PORTAL", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                } else {
                    // Display User Links (ONLY TITLES clearly displayed as requested)
                    items(userLinks, key = { it.id }) { link ->
                        UserAssignedTitleCard(
                            link = link,
                            onClick = {
                                activeOpenedLink = link
                            }
                        )
                    }
                }
            }
        }
    } else {
        // --- SCREEN 2: IN-APP SECURE WEB VIEWER (No Browser button, No Auto-refresh loop) ---
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(GateBlack)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Navigation Bar for active study session
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = GateCardElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GateBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back to Title List button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    activeOpenedLink = null
                                    activeFallbackUrl = null
                                    webViewInstance = null
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Links",
                                tint = GateRedLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BACK TO LINKS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GateRedLight
                            )
                        }

                        // Resource Title Name
                        Text(
                            text = activeOpenedLink?.title ?: "Study Session",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = GateWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        // Action Buttons: Reload and Logout
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { webViewInstance?.reload() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload",
                                    tint = GateTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.userLogout() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = "Logout",
                                    tint = GateRedLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Web Page Loading Progress Bar
                if (isLoading && webProgress < 100) {
                    LinearProgressIndicator(
                        progress = { webProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp),
                        color = GateRedPrimary,
                        trackColor = GateDarkGray.copy(alpha = 0.3f)
                    )
                }

                // In-App WebView
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    mediaPlaybackRequiresUserGesture = false
                                    allowFileAccess = true
                                    allowContentAccess = true
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    userAgentString = userAgentString.replace("; wv", "")
                                }

                                CookieManager.getInstance().setAcceptCookie(true)
                                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                                setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                                    handlePdfOrDownload(url, userAgent, contentDisposition, mimetype)
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        webProgress = newProgress
                                        if (newProgress >= 100) {
                                            isLoading = false
                                        }
                                        canGoBack = view?.canGoBack() == true
                                        canGoForward = view?.canGoForward() == true

                                        // Instant replacement on every progress milestone
                                        if (newProgress > 25) {
                                            injectThorToSscReplacer(view)
                                        }
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isLoading = true
                                        canGoBack = view?.canGoBack() == true
                                        canGoForward = view?.canGoForward() == true
                                        injectThorToSscReplacer(view)
                                    }

                                    override fun onPageCommitVisible(view: WebView?, url: String?) {
                                        super.onPageCommitVisible(view, url)
                                        // Run instantly as soon as page DOM is first painted
                                        injectThorToSscReplacer(view)
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        canGoBack = view?.canGoBack() == true
                                        canGoForward = view?.canGoForward() == true
                                        injectThorToSscReplacer(view)
                                    }

                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        val targetUrl = request?.url?.toString() ?: return false

                                        val isPdfOrDoc = targetUrl.endsWith(".pdf", ignoreCase = true) ||
                                                targetUrl.contains(".pdf?", ignoreCase = true) ||
                                                targetUrl.contains("/pdf/", ignoreCase = true) ||
                                                targetUrl.contains("format=pdf", ignoreCase = true) ||
                                                targetUrl.contains("attachment", ignoreCase = true) && targetUrl.contains("pdf", ignoreCase = true)

                                        if (isPdfOrDoc) {
                                            handlePdfOrDownload(targetUrl, settings.userAgentString, null, "application/pdf")
                                            return true
                                        }
                                        // Keep navigation inside WebView
                                        return false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        // Handle gracefully
                                    }
                                }

                                loadUrl(currentUrlToLoad)
                                webViewInstance = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Loading Spinner Overlay
                    if (isLoading && webProgress < 30) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GateBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = GateRedPrimary,
                                    modifier = Modifier.size(36.dp),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Loading ${activeOpenedLink?.title ?: "Resource"}...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GateTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Floating Navigation Pill for In-page back/forward (Without Browser option!)
            if (canGoBack || canGoForward) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .background(GateDarkGray.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                            .border(1.dp, GateBorderRed, RoundedCornerShape(20.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (canGoBack) {
                            IconButton(
                                onClick = { webViewInstance?.goBack() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = GateWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (canGoForward) {
                            IconButton(
                                onClick = { webViewInstance?.goForward() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Forward",
                                    tint = GateWhite,
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

@Composable
private fun UserAssignedTitleCard(
    link: UserLinkEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GateBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("user_link_${link.id}"),
        color = GateCardElevated,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(GateRedDark.copy(alpha = 0.7f), GateDarkGray)
                            )
                        )
                        .border(1.dp, GateRedPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = GateRedLight,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    // ONLY THE TITLE PROMINENTLY DISPLAYED
                    Text(
                        text = link.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GateWhite,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (link.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = link.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = GateTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Assigned: ${formatDate(link.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GateTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action Button to Open
            Surface(
                color = GateRedPrimary,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable { onClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OPEN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = GateWhite
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = GateWhite,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OutlinedButtonWithIcon(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, GateRedPrimary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag("student_logout_button"),
        color = GateRedDark.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GateRedLight,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = GateWhite
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Instantly replaces all instances of "THOR" or "Thor" with "SSC" on the webpage.
 * Executes in microseconds and monitors any dynamic scrolling/lazy-loading via MutationObserver.
 */
private fun injectThorToSscReplacer(webView: WebView?) {
    if (webView == null) return
    val jsCode = """
        (function() {
            if (window._pwThorReplacerInstalled) {
                if (window._replaceThorText) window._replaceThorText();
                return;
            }
            window._pwThorReplacerInstalled = true;

            function replaceInNode(node) {
                if (!node) return;
                if (node.nodeType === Node.TEXT_NODE) {
                    var val = node.nodeValue;
                    if (val && /THOR/i.test(val)) {
                        node.nodeValue = val
                            .replace(/\bPW\s*THOR\b/gi, 'PW SSC')
                            .replace(/\bTHOR\b/g, 'SSC')
                            .replace(/\bThor\b/g, 'SSC')
                            .replace(/\bthor\b/g, 'ssc');
                    }
                } else if (node.nodeType === Node.ELEMENT_NODE) {
                    var tag = node.tagName.toLowerCase();
                    if (tag !== 'script' && tag !== 'style' && tag !== 'textarea' && tag !== 'input') {
                        for (var i = 0; i < node.childNodes.length; i++) {
                            replaceInNode(node.childNodes[i]);
                        }
                    }
                }
            }

            window._replaceThorText = function() {
                if (document.body) {
                    replaceInNode(document.body);
                }
                if (document.title && /THOR/i.test(document.title)) {
                    document.title = document.title
                        .replace(/\bPW\s*THOR\b/gi, 'PW SSC')
                        .replace(/\bTHOR\b/g, 'SSC')
                        .replace(/\bThor\b/g, 'SSC');
                }
            };

            // Run immediately
            window._replaceThorText();

            // Real-time MutationObserver for infinite scrolls, tabs, and dynamic components
            if (window.MutationObserver && document.documentElement) {
                var observer = new MutationObserver(function(mutations) {
                    for (var i = 0; i < mutations.length; i++) {
                        var m = mutations[i];
                        if (m.type === 'childList') {
                            for (var j = 0; j < m.addedNodes.length; j++) {
                                replaceInNode(m.addedNodes[j]);
                            }
                        } else if (m.type === 'characterData') {
                            replaceInNode(m.target);
                        }
                    }
                });
                observer.observe(document.documentElement, {
                    childList: true,
                    subtree: true,
                    characterData: true
                });
            }
        })();
    """.trimIndent()

    webView.evaluateJavascript(jsCode, null)
}
