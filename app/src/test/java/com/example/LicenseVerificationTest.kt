package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.InventoryRepository
import com.example.domain.license.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LicenseVerificationTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: InventoryRepository

    private val allTwentyKeys = listOf(
        "H40-PRO-7K4M-X9QP-2F8D",
        "H40-PRO-M6TA-4R2K-8P7W",
        "H40-PRO-Q8NX-5D3L-K7V2",
        "H40-PRO-Z4RC-9H6P-T2YM",
        "H40-PRO-3WFK-8N5A-J9RX",
        "H40-PRO-V7DL-2Q8M-4KPT",
        "H40-PRO-6BXR-Y3F9-N8CW",
        "H40-PRO-P5HM-7T2K-X4QA",
        "H40-PRO-R9VK-3M6D-W8LP",
        "H40-PRO-2NQF-6X7A-H5ZM",
        "H40-PRO-K8TY-4P3R-V6NX",
        "H40-PRO-5JWC-9L2H-Q7MK",
        "H40-PRO-X3DA-8F6T-R4VP",
        "H40-PRO-N7GM-5K9X-C2WL",
        "H40-PRO-4QZP-Y8R3-M6HF",
        "H40-PRO-T6KN-2V5C-9XJW",
        "H40-PRO-8LFR-M4Q7-D3YK",
        "H40-PRO-W2HX-7C9N-P5VA",
        "H40-PRO-J9MQ-6T4K-Z8RD",
        "H40-PRO-C5VP-3N7L-X9QF"
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = InventoryRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `all 20 predefined PRO keys validate cryptographically and activate`() = runBlocking {
        assertEquals("Must test exactly 20 keys", 20, allTwentyKeys.size)

        for ((index, rawKey) in allTwentyKeys.withIndex()) {
            val result = LicenseManager.activateLicense(
                rawKey = rawKey,
                context = context,
                repository = repository,
                customDeviceId = "DEVICE_TEST_$index"
            )

            assertTrue("Key $rawKey should be valid", result.isValid)
            assertEquals("Key $rawKey should be ACTIVE", LicenseStatus.ACTIVE, result.status)
            assertNotNull("Masked key must not be null", result.maskedKey)
            assertTrue("Masked key must mask middle characters", result.maskedKey!!.contains("••••"))
            assertFalse("Masked key must not reveal full key", result.maskedKey == rawKey)
        }

        // Verify all 20 records exist in the repository
        val records = repository.getAllLicenseRecordsDirect()
        assertEquals(20, records.size)
    }

    @Test
    fun `key normalization handles lowercase and whitespace`() = runBlocking {
        val messyKey = "  h40 - pro - 7k4m - x9qp - 2f8d  "
        val normalized = LicenseSecurity.normalizeKey(messyKey)
        assertEquals("H40-PRO-7K4M-X9QP-2F8D", normalized)

        val result = LicenseManager.activateLicense(
            rawKey = messyKey,
            context = context,
            repository = repository,
            customDeviceId = "NORM_DEVICE_1"
        )
        assertTrue("Normalized messy key must activate", result.isValid)
        assertEquals(LicenseStatus.ACTIVE, result.status)
        assertEquals("H40-PRO-••••-••••-2F8D", result.maskedKey)
    }

    @Test
    fun `invalid key is rejected with proper error`() = runBlocking {
        val invalidKeys = listOf(
            "INVALID-KEY-1234",
            "H40-PRO-0000-0000-0000",
            "H40-PRO-7K4M-X9QP-9999",
            ""
        )

        for (invalidKey in invalidKeys) {
            val result = LicenseManager.activateLicense(
                rawKey = invalidKey,
                context = context,
                repository = repository
            )
            assertFalse("Invalid key must not activate: $invalidKey", result.isValid)
            assertEquals(LicenseStatus.INVALID, result.status)
            assertEquals("Invalid license key", result.errorMessage)
        }
    }

    @Test
    fun `device limit of 2 devices is strictly enforced`() = runBlocking {
        val key = "H40-PRO-M6TA-4R2K-8P7W"

        // Device 1
        val res1 = LicenseManager.activateLicense(
            rawKey = key,
            context = context,
            repository = repository,
            customDeviceId = "PHONE_01"
        )
        assertTrue(res1.isValid)
        assertEquals(1, res1.activatedDevices)

        // Device 2
        val res2 = LicenseManager.activateLicense(
            rawKey = key,
            context = context,
            repository = repository,
            customDeviceId = "TABLET_02"
        )
        assertTrue(res2.isValid)
        assertEquals(2, res2.activatedDevices)

        // Device 3 (Must fail)
        val res3 = LicenseManager.activateLicense(
            rawKey = key,
            context = context,
            repository = repository,
            customDeviceId = "LAPTOP_03"
        )
        assertFalse("Third device must be rejected", res3.isValid)
        assertEquals(LicenseStatus.ACTIVATION_LIMIT_REACHED, res3.status)
        assertTrue(res3.errorMessage!!.contains("maximum 2 devices"))

        // Re-activating existing Device 1 should succeed without incrementing count
        val res1Again = LicenseManager.activateLicense(
            rawKey = key,
            context = context,
            repository = repository,
            customDeviceId = "PHONE_01"
        )
        assertTrue("Existing device re-activation should succeed", res1Again.isValid)
        assertEquals(2, res1Again.activatedDevices)
    }

    @Test
    fun `revocation and suspension admin controls`() = runBlocking {
        val key = "H40-PRO-Q8NX-5D3L-K7V2"
        val activation = LicenseManager.activateLicense(
            rawKey = key,
            context = context,
            repository = repository,
            customDeviceId = "ADMIN_DEVICE_1"
        )
        assertTrue(activation.isValid)
        val licenseId = activation.licenseId!!

        // Revoke license
        val revoked = LicenseManager.revokeLicense(licenseId, repository)
        assertTrue(revoked)

        // Attempt activation on revoked license
        val attemptOnRevoked = LicenseManager.activateLicense(
            rawKey = key,
            context = context,
            repository = repository,
            customDeviceId = "ADMIN_DEVICE_2"
        )
        assertFalse(attemptOnRevoked.isValid)
        assertEquals(LicenseStatus.REVOKED, attemptOnRevoked.status)
        assertEquals("License has been revoked.", attemptOnRevoked.errorMessage)

        // Reactivate license
        LicenseManager.reactivateLicense(licenseId, repository)
        val attemptAfterReactivate = LicenseManager.activateLicense(
            rawKey = key,
            context = context,
            repository = repository,
            customDeviceId = "ADMIN_DEVICE_1"
        )
        assertTrue("Reactivated license must succeed", attemptAfterReactivate.isValid)
        assertEquals(LicenseStatus.ACTIVE, attemptAfterReactivate.status)

        // Suspend license
        LicenseManager.suspendLicense(licenseId, repository)
        val attemptOnSuspended = LicenseManager.activateLicense(
            rawKey = key,
            context = context,
            repository = repository,
            customDeviceId = "ADMIN_DEVICE_1"
        )
        assertFalse(attemptOnSuspended.isValid)
        assertEquals(LicenseStatus.SUSPENDED, attemptOnSuspended.status)
    }

    @Test
    fun `offline activation token integrity check`() = runBlocking {
        val key = "H40-PRO-Z4RC-9H6P-T2YM"
        val res = LicenseManager.activateLicense(key, context, repository, "OFFLINE_DEV_01")
        assertTrue(res.isValid)

        val activeLic = repository.getLicenseDirect()
        assertNotNull(activeLic)
        assertTrue(activeLic!!.activationToken.isNotBlank())
        assertTrue(LicenseManager.isLicenseActive(activeLic))

        val isTokenValid = LicenseSecurity.verifyLocalActivationToken(
            token = activeLic.activationToken,
            licenseId = activeLic.licenseId,
            keyId = LicenseSecurity.computeKeyId(key),
            deviceId = activeLic.deviceId,
            timestamp = activeLic.activatedAt
        )
        assertTrue("Local offline activation token must be valid", isTokenValid)
    }
}
