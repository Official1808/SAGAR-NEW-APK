package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class EmailService(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("pwgate_email_settings", Context.MODE_PRIVATE)

    companion object {
        const val TAG = "EmailService"
        const val DEFAULT_SMTP_SENDER = "mesagarmeena@gmail.com"
        const val DEFAULT_RECIPIENT_EMAIL = "mesagarmeena@gmail.com"
        const val DEFAULT_ADMIN_EMAIL = "mesagarmeena@gmail.com"
        const val DEFAULT_VERCEL_URL = "https://sagar-new-apk.vercel.app"
        const val SMTP_HOST = "smtp.gmail.com"
        const val SMTP_PORT = 465
    }

    var resendApiKey: String
        get() = prefs.getString("resend_api_key", "")?.trim() ?: ""
        set(value) {
            prefs.edit().putString("resend_api_key", value.trim()).apply()
        }

    var smtpSenderEmail: String
        get() = prefs.getString("smtp_sender_email", DEFAULT_SMTP_SENDER)?.trim()?.ifBlank { DEFAULT_SMTP_SENDER } ?: DEFAULT_SMTP_SENDER
        set(value) {
            prefs.edit().putString("smtp_sender_email", value.trim()).apply()
        }

    var recipientEmail: String
        get() = prefs.getString("recipient_email", prefs.getString("admin_email", DEFAULT_RECIPIENT_EMAIL) ?: DEFAULT_RECIPIENT_EMAIL)?.trim()?.ifBlank { DEFAULT_RECIPIENT_EMAIL } ?: DEFAULT_RECIPIENT_EMAIL
        set(value) {
            prefs.edit().putString("recipient_email", value.trim()).putString("admin_email", value.trim()).apply()
        }

    // Alias for backward compatibility
    var adminEmail: String
        get() = recipientEmail
        set(value) {
            recipientEmail = value
        }

    var appPassword: String
        get() = prefs.getString("gmail_app_password", "")?.trim()?.replace(" ", "") ?: ""
        set(value) {
            prefs.edit().putString("gmail_app_password", value.trim().replace(" ", "")).apply()
        }

    var vercelBackendUrl: String
        get() = prefs.getString("vercel_backend_url", DEFAULT_VERCEL_URL)?.trim()?.ifBlank { DEFAULT_VERCEL_URL } ?: DEFAULT_VERCEL_URL
        set(value) {
            prefs.edit().putString("vercel_backend_url", value.trim()).apply()
        }

    fun isConfigured(): Boolean = resendApiKey.isNotBlank() || (appPassword.isNotBlank() && smtpSenderEmail.isNotBlank())

    /**
     * Send access request email via direct HTTP mail dispatch and Gmail SMTP (if configured)
     */
    suspend fun sendAccessRequestEmail(
        username: String,
        requestType: String,
        deviceType: String,
        operatingSystem: String,
        ipAddress: String,
        region: String,
        approvalToken: String,
        denyToken: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val targetRecipient = recipientEmail
        val senderAccount = smtpSenderEmail
        val currentPassword = appPassword

        val requestTitle = if (requestType == "SIGNUP") "New Account Registration Request" else "New Device Login Request"
        val subject = "PW SARA Alert: $requestTitle from $username"
        val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a (z)", Locale.getDefault()).format(Date())

        val encodedUsername = try { java.net.URLEncoder.encode(username, "UTF-8") } catch (e: Exception) { username }
        val baseVercel = vercelBackendUrl.trimEnd('/')
        val vercelApproveUrl = if (baseVercel.isNotBlank()) {
            val endpoint = if (baseVercel.endsWith("/api/action")) baseVercel else "$baseVercel/api/action"
            "$endpoint?action=approve&token=$approvalToken&username=$encodedUsername&type=$requestType"
        } else ""
        val vercelDenyUrl = if (baseVercel.isNotBlank()) {
            val endpoint = if (baseVercel.endsWith("/api/action")) baseVercel else "$baseVercel/api/action"
            "$endpoint?action=deny&token=$denyToken&username=$encodedUsername&type=$requestType"
        } else ""

        val htmlBody = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8">
              <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0d0e12; color: #f3f4f6; margin: 0; padding: 20px; }
                .container { max-width: 580px; margin: 0 auto; background: #16181f; border-radius: 12px; border: 1px solid #dc2626; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }
                .header { background: linear-gradient(135deg, #7f1d1d, #991b1b); padding: 24px 20px; text-align: center; }
                .header h1 { color: #ffffff; margin: 0; font-size: 22px; font-weight: 800; letter-spacing: 1px; }
                .header p { color: #fecaca; margin: 6px 0 0; font-size: 13px; }
                .content { padding: 24px 20px; }
                .badge { display: inline-block; background: #dc2626; color: #fff; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: bold; margin-bottom: 16px; }
                .info-table { width: 100%; border-collapse: collapse; margin: 16px 0; }
                .info-table td { padding: 10px 8px; border-bottom: 1px solid #232734; font-size: 14px; }
                .info-table td.label { color: #9ca3af; font-weight: 600; width: 38%; }
                .info-table td.value { color: #f9fafb; font-weight: 700; }
                .actions { margin-top: 24px; text-align: center; background: #111319; padding: 20px; border-radius: 10px; border: 1px solid #232734; }
                .btn { display: inline-block; padding: 14px 28px; border-radius: 8px; font-size: 16px; font-weight: bold; text-decoration: none; margin: 8px; }
                .btn-approve { background-color: #16a34a; color: #ffffff !important; }
                .btn-deny { background-color: #dc2626; color: #ffffff !important; }
                .tokens { margin-top: 20px; background: #0b0c10; padding: 14px; border-radius: 8px; border: 1px dashed #374151; font-family: monospace; font-size: 12px; color: #9ca3af; word-break: break-all; }
                .footer { padding: 16px; text-align: center; font-size: 11px; color: #6b7280; border-top: 1px solid #232734; background: #0f1117; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>PW SARA SECURITY PORTAL</h1>
                  <p>Real-Time Student Access Control Alert</p>
                </div>
                <div class="content">
                  <span class="badge">$requestType ACTION REQUIRED</span>
                  <h2 style="color: #ffffff; margin-top: 0;">$requestTitle</h2>
                  <p style="color: #9ca3af; font-size: 14px; line-height: 1.5;">
                    A student has submitted an access verification request. Please review the hardware parameters below and authorize or deny access:
                  </p>

                  <table class="info-table">
                    <tr><td class="label">Student Username:</td><td class="value" style="color:#ef4444; font-size: 16px;">$username</td></tr>
                    <tr><td class="label">Request Type:</td><td class="value">$requestType</td></tr>
                    <tr><td class="label">Hardware Device:</td><td class="value">$deviceType</td></tr>
                    <tr><td class="label">Operating System:</td><td class="value">$operatingSystem</td></tr>
                    <tr><td class="label">IP Address:</td><td class="value">$ipAddress</td></tr>
                    <tr><td class="label">Location:</td><td class="value">$region</td></tr>
                    <tr><td class="label">Timestamp:</td><td class="value">$formattedDate</td></tr>
                  </table>

                  <div class="actions">
                    <p style="color: #ffffff; font-weight: bold; margin-top: 0; margin-bottom: 14px; font-size: 16px;">Instant One-Click Decision:</p>
                    
                    ${if (vercelApproveUrl.isNotBlank()) """
                    <table role="presentation" border="0" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%;">
                      <tr>
                        <td align="center" style="padding: 6px;">
                          <a href="$vercelApproveUrl" class="btn btn-approve" style="background-color:#16a34a; color:#ffffff !important; display:inline-block; padding:14px 28px; border-radius:8px; font-size:16px; font-weight:bold; text-decoration:none;">✔ APPROVE ACCESS</a>
                          <a href="$vercelDenyUrl" class="btn btn-deny" style="background-color:#dc2626; color:#ffffff !important; display:inline-block; padding:14px 28px; border-radius:8px; font-size:16px; font-weight:bold; text-decoration:none;">✖ DENY ACCESS</a>
                        </td>
                      </tr>
                    </table>
                    <p style="font-size:12px; color:#9ca3af; margin: 10px 0 0 0;">Connected Server: <span style="color:#60a5fa;">$baseVercel</span></p>
                    """ else """
                    <div style="margin-bottom: 12px;">
                      <a href="intent://auth?token=$approvalToken#Intent;scheme=pwgate;package=com.aistudio.pwgate.kxmpzq;end" class="btn btn-approve">✔ OPEN APP & APPROVE</a>
                      <a href="intent://auth?token=$denyToken#Intent;scheme=pwgate;package=com.aistudio.pwgate.kxmpzq;end" class="btn btn-deny">✖ OPEN APP & DENY</a>
                    </div>
                    """}

                    <div style="margin-top: 14px;">
                      <a href="intent://auth?token=$approvalToken#Intent;scheme=pwgate;package=com.aistudio.pwgate.kxmpzq;end" style="color:#93c5fd; font-size:12px; text-decoration:underline;">📱 Or open directly in PW SARA Android App</a>
                    </div>
                  </div>

                  <div class="tokens" style="margin-top: 20px; background: #111319; border: 1px solid #232734; border-radius: 8px; padding: 16px; text-align: left;">
                    <strong style="color: #ffffff; font-size: 13px;">⚡ Quick Tokens & Direct App Info:</strong><br>
                    <p style="margin: 6px 0; color: #d1d5db; font-size: 13px; line-height: 1.5;">
                      • <strong>Approval Token:</strong> <code style="background:#1f2430; color:#22c55e; padding:3px 6px; border-radius:4px; font-weight:bold;">$approvalToken</code><br>
                      • <strong>Denial Token:</strong> <code style="background:#1f2430; color:#ef4444; padding:3px 6px; border-radius:4px; font-weight:bold;">$denyToken</code><br>
                      • <strong>In-App One-Tap:</strong> Open PW SARA App &rarr; Admin Dashboard &rarr; <em>Pending Requests</em> tab &rarr; Tap <span style="color:#22c55e; font-weight:bold;">APPROVE</span>.
                    </p>
                  </div>
                </div>
                <div class="footer">
                  PW SARA Identity Protection Engine • Automated System Notification
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()

        var resendSuccess = false
        var httpSuccess = false
        var smtpSuccess = false
        var lastError: Exception? = null

        // 1. Resend API Dispatcher (Instant HTML delivery with high deliverability)
        if (resendApiKey.isNotBlank()) {
            try {
                resendSuccess = sendResendEmail(
                    apiKey = resendApiKey,
                    toEmail = targetRecipient,
                    subject = subject,
                    htmlContent = htmlBody
                )
            } catch (e: Exception) {
                Log.e(TAG, "Resend API dispatch error", e)
                lastError = e
            }
        }

        // 2. Direct HTTP Email Dispatchers
        try {
            httpSuccess = sendHttpEmail(
                toEmail = targetRecipient,
                subject = subject,
                username = username,
                requestType = requestType,
                deviceType = deviceType,
                operatingSystem = operatingSystem,
                ipAddress = ipAddress,
                region = region,
                approvalToken = approvalToken,
                denyToken = denyToken,
                formattedDate = formattedDate,
                approveUrl = vercelApproveUrl,
                denyUrl = vercelDenyUrl
            )
        } catch (e: Exception) {
            Log.e(TAG, "Direct HTTP email dispatch error", e)
            if (lastError == null) lastError = e
        }

        // 3. If Gmail App Password is configured, also send via Gmail SMTP SSL (Port 465)
        if (currentPassword.isNotBlank()) {
            try {
                sendSmtpEmail(
                    toEmail = targetRecipient,
                    fromEmail = senderAccount,
                    password = currentPassword,
                    subject = subject,
                    htmlContent = htmlBody
                )
                smtpSuccess = true
            } catch (e: Exception) {
                Log.e(TAG, "Error sending email via Gmail SMTP", e)
                if (lastError == null) lastError = e
            }
        }

        if (resendSuccess || httpSuccess || smtpSuccess) {
            Result.success("Email alert successfully dispatched to $targetRecipient")
        } else {
            Result.failure(lastError ?: IllegalStateException("Failed to deliver security alert email. Please check your Resend API Key or Gmail App Password."))
        }
    }

    /**
     * Send email via Resend API (api.resend.com)
     */
    private fun sendResendEmail(
        apiKey: String,
        toEmail: String,
        subject: String,
        htmlContent: String,
        fromEmail: String = "PW SARA <onboarding@resend.dev>"
    ): Boolean {
        try {
            val url = URL("https://api.resend.com/emails")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val json = org.json.JSONObject().apply {
                put("from", fromEmail)
                put("to", org.json.JSONArray().apply { put(toEmail.trim()) })
                put("subject", subject)
                put("html", htmlContent)
            }

            conn.outputStream.use { os ->
                os.write(json.toString().toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = conn.responseCode
            val responseBody = try {
                conn.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            Log.d(TAG, "Resend API response ($code): $responseBody")
            return code in 200..299
        } catch (e: Exception) {
            Log.e(TAG, "Resend API call exception", e)
            return false
        }
    }

    /**
     * Send email via multiple HTTP Form endpoints (FormSubmit JSON & Form-URLEncoded)
     */
    private fun sendHttpEmail(
        toEmail: String,
        subject: String,
        username: String,
        requestType: String,
        deviceType: String,
        operatingSystem: String,
        ipAddress: String,
        region: String,
        approvalToken: String,
        denyToken: String,
        formattedDate: String,
        approveUrl: String = "",
        denyUrl: String = ""
    ): Boolean {
        val effectiveApproveUrl = if (approveUrl.isNotBlank()) approveUrl else "$DEFAULT_VERCEL_URL/api/action?action=approve&token=$approvalToken&username=$username"
        val effectiveDenyUrl = if (denyUrl.isNotBlank()) denyUrl else "$DEFAULT_VERCEL_URL/api/action?action=deny&token=$denyToken&username=$username"

        // Attempt 1: FormSubmit AJAX JSON endpoint
        try {
            val url = URL("https://formsubmit.co/ajax/$toEmail")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("User-Agent", "PWGate-SecurityEngine/2.0")
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val json = org.json.JSONObject().apply {
                put("_subject", subject)
                put("_template", "table")
                put("_captcha", "false")
                put("Student Username", username)
                put("Request Type", requestType)
                put("Hardware Device", deviceType)
                put("Operating System", operatingSystem)
                put("IP Address", ipAddress)
                put("Location", region)
                put("Timestamp", formattedDate)
                put("✔ APPROVE ACCESS (Instant)", effectiveApproveUrl)
                put("✖ DENY ACCESS (Instant)", effectiveDenyUrl)
                put("Approval Token", approvalToken)
                put("Denial Token", denyToken)
                put("PW SARA Notice", "Click Approve or Deny link above to authorize the student instantly.")
            }

            conn.outputStream.use { os ->
                os.write(json.toString().toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = conn.responseCode
            val responseText = try {
                conn.inputStream.bufferedReader().use { it.readText() }
            } catch (_: Exception) {
                conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            Log.d(TAG, "FormSubmit AJAX Response ($code): $responseText")
            if (code in 200..299) return true
        } catch (e: Exception) {
            Log.e(TAG, "FormSubmit AJAX dispatch failed, trying standard endpoint", e)
        }

        // Attempt 2: FormSubmit Standard URL-Encoded POST
        try {
            val url = URL("https://formsubmit.co/$toEmail")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            conn.setRequestProperty("User-Agent", "PWGate-SecurityEngine/2.0")
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val postData = buildString {
                append("_subject=").append(URLEncoder.encode(subject, "UTF-8"))
                append("&_captcha=false")
                append("&_template=table")
                append("&Student_Username=").append(URLEncoder.encode(username, "UTF-8"))
                append("&Request_Type=").append(URLEncoder.encode(requestType, "UTF-8"))
                append("&Device=").append(URLEncoder.encode(deviceType, "UTF-8"))
                append("&Operating_System=").append(URLEncoder.encode(operatingSystem, "UTF-8"))
                append("&IP_Address=").append(URLEncoder.encode(ipAddress, "UTF-8"))
                append("&Location=").append(URLEncoder.encode(region, "UTF-8"))
                append("&Timestamp=").append(URLEncoder.encode(formattedDate, "UTF-8"))
                append("&APPROVE_ACCESS=").append(URLEncoder.encode(effectiveApproveUrl, "UTF-8"))
                append("&DENY_ACCESS=").append(URLEncoder.encode(effectiveDenyUrl, "UTF-8"))
                append("&Approval_Token=").append(URLEncoder.encode(approvalToken, "UTF-8"))
                append("&Denial_Token=").append(URLEncoder.encode(denyToken, "UTF-8"))
            }

            conn.outputStream.use { os ->
                os.write(postData.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = conn.responseCode
            Log.d(TAG, "FormSubmit standard POST response code: $code")
            return code in 200..399
        } catch (e: Exception) {
            Log.e(TAG, "FormSubmit standard POST failed", e)
        }

        return false
    }

    /**
     * Send a comprehensive test email to verify credentials and report diagnostics
     */
    suspend fun sendTestEmail(): Result<String> = withContext(Dispatchers.IO) {
        val targetRecipient = recipientEmail
        val senderAccount = smtpSenderEmail
        val currentPassword = appPassword

        val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date())
        val subject = "PW SARA: Security Alert Test Delivery"

        val diagnostics = StringBuilder()

        // 1. Try Resend API if API Key is configured
        if (resendApiKey.isNotBlank()) {
            val resendHtml = """
                <!DOCTYPE html>
                <html>
                <body style="background:#0d0e12; color:#fff; font-family:sans-serif; padding:20px;">
                  <div style="max-width:500px; margin:0 auto; background:#16181f; padding:24px; border-radius:10px; border:1px solid #3b82f6;">
                    <h2 style="color:#60a5fa; margin-top:0;">✔ Resend API Connection Verified</h2>
                    <p style="color:#e5e7eb;">Your Resend API Key is working perfectly and connected with PW SARA!</p>
                    <p style="color:#9ca3af; font-size:13px;">Timestamp: $formattedDate</p>
                    <p style="color:#9ca3af; font-size:13px;">Recipient Inbox: $targetRecipient</p>
                    <p style="color:#9ca3af; font-size:13px;">Vercel Gateway: $vercelBackendUrl</p>
                  </div>
                </body>
                </html>
            """.trimIndent()

            val resendOk = sendResendEmail(
                apiKey = resendApiKey,
                toEmail = targetRecipient,
                subject = "✔ PW SARA: Resend API Test Success",
                htmlContent = resendHtml
            )
            if (resendOk) {
                diagnostics.append("✔ Resend API: Email delivered successfully to $targetRecipient!\n")
            } else {
                diagnostics.append("✖ Resend API: Failed. Please verify your Resend API Key.\n")
            }
        } else {
            diagnostics.append("ℹ Resend API: Not configured yet. (Add your Resend API Key for direct instant delivery)\n")
        }

        // 2. Try Direct HTTP Webhook
        val httpOk = sendHttpEmail(
            toEmail = targetRecipient,
            subject = subject,
            username = "ADMIN_TEST_VERIFICATION",
            requestType = "TEST_VERIFICATION",
            deviceType = "Admin Security Console",
            operatingSystem = "Android Security Engine",
            ipAddress = "127.0.0.1",
            region = "Secure Security Console",
            approvalToken = "tok_test_approve",
            denyToken = "tok_test_deny",
            formattedDate = formattedDate
        )

        if (httpOk) {
            diagnostics.append("✔ Direct Webhook Dispatch: Sent successfully to $targetRecipient.\n")
        } else {
            diagnostics.append("✖ Direct Webhook Dispatch: Failed or pending activation.\n")
        }

        // 2. Try Gmail SMTP if App Password is provided
        if (currentPassword.isNotBlank()) {
            val htmlBody = """
                <!DOCTYPE html>
                <html>
                <body style="background:#0d0e12; color:#fff; font-family:sans-serif; padding:20px;">
                  <div style="max-width:500px; margin:0 auto; background:#16181f; padding:24px; border-radius:10px; border:1px solid #22c55e;">
                    <h2 style="color:#22c55e; margin-top:0;">✔ Gmail SMTP Connection Verified</h2>
                    <p style="color:#e5e7eb;">Your Gmail SMTP connection is active and configured correctly with PW SARA!</p>
                    <p style="color:#9ca3af; font-size:13px;">Timestamp: $formattedDate</p>
                    <p style="color:#9ca3af; font-size:13px;">Sent From (SMTP Auth): $senderAccount</p>
                    <p style="color:#9ca3af; font-size:13px;">Delivered To: $targetRecipient</p>
                    <p style="color:#9ca3af; font-size:13px;">Protocol: Google SMTP SSL (Port 465)</p>
                  </div>
                </body>
                </html>
            """.trimIndent()

            try {
                sendSmtpEmail(
                    toEmail = targetRecipient,
                    fromEmail = senderAccount,
                    password = currentPassword,
                    subject = subject,
                    htmlContent = htmlBody
                )
                diagnostics.append("✔ Gmail SMTP SSL (Port 465): Delivered directly from $senderAccount into $targetRecipient inbox!")
                return@withContext Result.success(diagnostics.toString())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send test email via SMTP", e)
                diagnostics.append("✖ Gmail SMTP Error: ${e.message}\n(Make sure you entered your 16-character Google App Password for $senderAccount from myaccount.google.com/apppasswords)")
            }
        } else {
            diagnostics.append("ℹ Gmail SMTP: Not active yet. Enter your 16-letter Google App Password in Settings for direct instant inbox delivery.")
        }

        if (httpOk) {
            Result.success(diagnostics.toString())
        } else {
            Result.failure(Exception(diagnostics.toString()))
        }
    }

    /**
     * Pure Java/Kotlin TLS SSL socket SMTP client for smtp.gmail.com:465
     */
    private fun sendSmtpEmail(
        toEmail: String,
        fromEmail: String,
        password: String,
        subject: String,
        htmlContent: String
    ) {
        val socketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        val socket = socketFactory.createSocket(SMTP_HOST, SMTP_PORT) as SSLSocket
        socket.soTimeout = 20000

        val reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.UTF_8))
        val writer = PrintWriter(OutputStreamWriter(socket.outputStream, Charsets.UTF_8), true)

        fun readResponse(): String {
            val response = StringBuilder()
            var line: String?
            while (true) {
                line = reader.readLine() ?: throw IllegalStateException("Unexpected end of stream from SMTP server")
                response.append(line).append("\n")
                // Check if last line of multiline response (4th char is space or line length is 3)
                if (line.length >= 3 && (line.length == 3 || line[3] == ' ')) {
                    break
                }
            }
            val res = response.toString().trim()
            Log.d(TAG, "SMTP <-- $res")
            return res
        }

        fun sendCommand(cmd: String, expectedCodePrefix: String) {
            Log.d(TAG, "SMTP --> (command)")
            writer.print("$cmd\r\n")
            writer.flush()
            val response = readResponse()
            if (!response.startsWith(expectedCodePrefix)) {
                throw IllegalStateException("SMTP error ($response). Check that your Gmail 16-letter App Password is correct.")
            }
        }

        try {
            // 1. Read Initial Server Greeting (220)
            val greeting = readResponse()
            if (!greeting.startsWith("220")) {
                throw IllegalStateException("SMTP server rejected connection: $greeting")
            }

            // 2. Send EHLO
            sendCommand("EHLO localhost", "250")

            // 3. Authenticate with AUTH LOGIN
            sendCommand("AUTH LOGIN", "334")

            // Send base64 username
            val userBase64 = Base64.encodeToString(fromEmail.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            sendCommand(userBase64, "334")

            // Send base64 app password
            val cleanPass = password.replace(" ", "").trim()
            val passBase64 = Base64.encodeToString(cleanPass.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            sendCommand(passBase64, "235")

            // 4. Set MAIL FROM & RCPT TO
            sendCommand("MAIL FROM:<$fromEmail>", "250")
            sendCommand("RCPT TO:<$toEmail>", "250")

            // 5. Send DATA
            sendCommand("DATA", "354")

            // 6. Write MIME headers and content
            writer.print("From: PW SARA Security <$fromEmail>\r\n")
            writer.print("To: $toEmail\r\n")
            writer.print("Subject: $subject\r\n")
            writer.print("MIME-Version: 1.0\r\n")
            writer.print("Content-Type: text/html; charset=UTF-8\r\n")
            writer.print("Content-Transfer-Encoding: 8bit\r\n")
            writer.print("\r\n")
            writer.print(htmlContent)
            writer.print("\r\n.\r\n")
            writer.flush()

            val dataResponse = readResponse()
            if (!dataResponse.startsWith("250")) {
                throw IllegalStateException("Failed to deliver email body: $dataResponse")
            }

            // 7. Quit
            try {
                sendCommand("QUIT", "221")
            } catch (_: Exception) {}

        } finally {
            try {
                socket.close()
            } catch (_: Exception) {}
        }
    }
}

