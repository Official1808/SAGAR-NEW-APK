package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.DeviceManager
import com.example.data.GateRepository
import com.example.data.LoginResult
import com.example.data.SignupResult
import com.example.security.SecurityUtils
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var deviceManager: DeviceManager
    private lateinit var repository: GateRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        deviceManager = DeviceManager(context)
        repository = GateRepository(db, deviceManager)
        runBlocking {
            repository.initializeDefaults()
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAppName() {
        val appName = context.getString(R.string.app_name)
        assertEquals("PW SARA", appName)
    }

    @Test
    fun testCaseInsensitiveUsernameNormalization() {
        // Test 9 & 10: Case-insensitive username and case-sensitive password
        val norm1 = SecurityUtils.normalizeUsername("SAGAR")
        val norm2 = SecurityUtils.normalizeUsername("sagar")
        val norm3 = SecurityUtils.normalizeUsername("SaGaR")
        assertEquals("sagar", norm1)
        assertEquals("sagar", norm2)
        assertEquals("sagar", norm3)

        // Password hashing & strict case sensitivity
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword("RAJANI", salt)
        assertTrue(SecurityUtils.verifyPassword("RAJANI", salt, hash))
        assertFalse(SecurityUtils.verifyPassword("rajani", salt, hash))
        assertFalse(SecurityUtils.verifyPassword("Rajani", salt, hash))
    }

    @Test
    fun testScenario1_NewUserSignupAndApprovalFlow() = runBlocking {
        // Test 1: New user signup -> admin approval -> login -> device registration -> access
        val signupRes = repository.signup("SagarMeena", "Secret1234")
        assertTrue(signupRes is SignupResult.Success)

        // Attempt login before admin approval -> should be PENDING
        val preApprovalLogin = repository.login("sagarmeena", "Secret1234")
        assertTrue(preApprovalLogin is LoginResult.UserPending)

        // Admin approves user
        val user = db.appDao().getUserByNormalizedUsername("sagarmeena")
        assertNotNull(user)
        repository.approveUser(user!!.id)

        // User logs in now -> User is approved, but device is new/pending
        val firstDeviceLogin = repository.login("SagarMeena", "Secret1234")
        assertTrue(firstDeviceLogin is LoginResult.DevicePending)

        // Admin approves device
        val device = db.appDao().getDevice(user.id, deviceManager.getCurrentDeviceIdentifier())
        assertNotNull(device)
        repository.approveDevice(device!!.id)

        // User logs in again -> Now both User and Device approved -> Access granted!
        val approvedLogin = repository.login("SAGARMEENA", "Secret1234")
        assertTrue(approvedLogin is LoginResult.Success)
        val success = approvedLogin as LoginResult.Success
        assertNotNull(success.sessionToken)
    }

    @Test
    fun testScenario2_MultiDeviceIsolation() = runBlocking {
        // Device approval & denial workflow
        repository.signup("Alice", "Pass1234")
        val user = db.appDao().getUserByNormalizedUsername("alice")!!
        repository.approveUser(user.id)

        // Device 1 login & approval
        val dev1Login = repository.login("alice", "Pass1234")
        assertTrue(dev1Login is LoginResult.DevicePending)
        val dev1 = db.appDao().getDevice(user.id, deviceManager.getCurrentDeviceIdentifier())!!
        repository.approveDevice(dev1.id)

        // Device 1 has access
        val dev1Success = repository.login("alice", "Pass1234")
        assertTrue(dev1Success is LoginResult.Success)

        // Admin denies Device
        repository.denyDevice(dev1.id)
        val devDeniedLogin = repository.login("alice", "Pass1234")
        assertTrue(devDeniedLogin is LoginResult.DeviceDenied)

        // Admin subsequently approves Device again
        repository.approveDevice(dev1.id)
        val devReapprovedLogin = repository.login("alice", "Pass1234")
        assertTrue(devReapprovedLogin is LoginResult.Success)
    }

    @Test
    fun testScenario3_BlockedUserAndWrongCredentials() = runBlocking {
        // Test 6 & 7: Wrong credentials & blocked user
        repository.signup("Bob", "BobPass123")
        val user = db.appDao().getUserByNormalizedUsername("bob")!!
        repository.approveUser(user.id)

        // Wrong password
        val wrongPassResult = repository.login("bob", "WrongPass")
        assertTrue(wrongPassResult is LoginResult.Error)

        // Block user
        repository.blockUser(user.id)
        val blockedResult = repository.login("bob", "BobPass123")
        assertTrue(blockedResult is LoginResult.UserBlocked)
    }

    @Test
    fun testScenario4_EmailSignedTokenApproval() = runBlocking {
        // Test 11: Email approval token processing
        repository.signup("Charlie", "CharliePass99")
        val req = db.appDao().getPendingRequestsFlow()
        val allReqs = db.appDao().getAllAccessRequestsFlow()
        val user = db.appDao().getUserByNormalizedUsername("charlie")!!
        val request = db.appDao().getPendingRequestForUser(user.id)!!

        // Process email approve token
        val result = repository.processEmailToken(request.approvalToken)
        assertTrue(result.isSuccess)

        val updatedUser = db.appDao().getUserById(user.id)!!
        assertEquals("APPROVED", updatedUser.status)
    }
}
