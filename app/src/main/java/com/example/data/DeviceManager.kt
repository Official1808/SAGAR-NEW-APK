package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class DeviceProfile(
    val id: String,
    val name: String,
    val identifier: String,
    val deviceType: String,
    val os: String,
    val ip: String,
    val region: String,
    val country: String
)

class DeviceManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("pwgate_device_prefs", Context.MODE_PRIVATE)

    private val defaultHardwareId: String
        get() {
            var id = prefs.getString("hardware_device_id", null)
            if (id == null) {
                val rawUuid = UUID.randomUUID().toString().replace("-", "").take(12).uppercase()
                id = "DEV-${Build.MANUFACTURER.take(3).uppercase()}-$rawUuid"
                prefs.edit().putString("hardware_device_id", id).apply()
            }
            return id
        }

    val hardwareProfile: DeviceProfile
        get() {
            val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
            val model = Build.MODEL
            val deviceDisplayName = if (model.startsWith(manufacturer, ignoreCase = true)) model else "$manufacturer $model"
            val osVersion = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
            
            return DeviceProfile(
                id = "REAL_HARDWARE",
                name = deviceDisplayName,
                identifier = defaultHardwareId,
                deviceType = deviceDisplayName,
                os = osVersion,
                ip = "103.212.144.58",
                region = "New Delhi, Delhi",
                country = "India"
            )
        }

    private val _activeProfile = MutableStateFlow(hardwareProfile)
    val activeProfile: StateFlow<DeviceProfile> = _activeProfile.asStateFlow()

    fun getCurrentDeviceIdentifier(): String = hardwareProfile.identifier
    fun getCurrentDeviceType(): String = hardwareProfile.deviceType
    fun getCurrentOS(): String = hardwareProfile.os
    fun getCurrentIP(): String = hardwareProfile.ip
    fun getCurrentRegion(): String = hardwareProfile.region
    fun getCurrentCountry(): String = hardwareProfile.country
    fun getAppVersion(): String = "1.2.0-PWGATE"
}
