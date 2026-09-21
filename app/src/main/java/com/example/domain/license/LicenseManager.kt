package com.example.domain.license

import android.content.Context
import com.example.BuildConfig
import com.example.data.entities.LicenseEntity
import com.example.data.entities.LicenseRecordEntity
import com.example.data.repository.InventoryRepository

/**
 * Enterprise Production License Manager for Hanouti 40 PRO.
 *
 * Implements cryptographic public-key verification, device authorization (up to 2 devices),
 * secure salted key identification, offline token verification, and administrative lifecycle controls.
 */
object LicenseManager {

    /**
     * Seeds the initial signed 20 production license records into the Room database if not yet present.
     */
    suspend fun ensureCatalogSeeded(repository: InventoryRepository) {
        val count = repository.getLicenseRecordCount()
        if (count < PredefinedLicenses.CATALOG.size) {
            val entities = PredefinedLicenses.CATALOG.map { record ->
                LicenseRecordEntity(
                    license_id = record.licenseId,
                    license_key_id = record.licenseKeyId,
                    license_type = record.licenseType,
                    status = LicenseStatus.AVAILABLE.name,
                    max_devices = record.maxDevices,
                    activated_devices = 0,
                    created_at = record.createdAt,
                    activated_at = null,
                    expires_at = record.expiresAt,
                    revoked_at = null,
                    last_seen = record.createdAt,
                    device_ids = "",
                    signature = record.signature
                )
            }
            repository.seedLicenseRecords(entities)
        }
    }

    /**
     * Activates a license key for this device.
     *
     * @param rawKey The user-entered key (case-insensitive, whitespace allowed)
     * @param context Android context for device identifier and preferences
     * @param repository The inventory repository
     * @param customDeviceId Optional device ID override for testing multi-device policies
     */
    suspend fun activateLicense(
        rawKey: String,
        context: Context,
        repository: InventoryRepository,
        customDeviceId: String? = null
    ): LicenseValidationResult {
        val normalized = LicenseSecurity.normalizeKey(rawKey)
        if (normalized.isBlank()) {
            return LicenseValidationResult(
                isValid = false,
                status = LicenseStatus.INVALID,
                errorMessage = "Invalid license key"
            )
        }

        // Compute salted key ID - raw key is NEVER written to DB or logs
        val candidateKeyId = LicenseSecurity.computeKeyId(normalized)

        // Ensure catalog is populated
        ensureCatalogSeeded(repository)

        // Query record from the production license database
        var record = repository.getLicenseRecordByKeyId(candidateKeyId)

        // If not found in DB but in catalog (e.g. fresh start), populate and find
        if (record == null) {
            val catalogMatch = PredefinedLicenses.CATALOG.find { it.licenseKeyId == candidateKeyId }
            if (catalogMatch != null) {
                record = LicenseRecordEntity(
                    license_id = catalogMatch.licenseId,
                    license_key_id = catalogMatch.licenseKeyId,
                    license_type = catalogMatch.licenseType,
                    status = LicenseStatus.AVAILABLE.name,
                    max_devices = catalogMatch.maxDevices,
                    activated_devices = 0,
                    created_at = catalogMatch.createdAt,
                    activated_at = null,
                    expires_at = catalogMatch.expiresAt,
                    revoked_at = null,
                    last_seen = System.currentTimeMillis(),
                    device_ids = "",
                    signature = catalogMatch.signature
                )
                repository.saveLicenseRecord(record)
            }
        }

        if (record == null) {
            return LicenseValidationResult(
                isValid = false,
                status = LicenseStatus.INVALID,
                errorMessage = "Invalid license key"
            )
        }

        // Cryptographic public-key verification of record payload
        val payload = "${record.license_id}|${record.license_key_id}|${record.license_type}|${record.max_devices}|${record.created_at}|${record.expires_at ?: Long.MAX_VALUE}"
        val signatureValid = LicenseSecurity.verifySignature(payload, record.signature)
        if (!signatureValid) {
            return LicenseValidationResult(
                isValid = false,
                status = LicenseStatus.INVALID,
                errorMessage = "Invalid license signature. Integrity check failed."
            )
        }

        // Check administrative status states
        when (record.status) {
            LicenseStatus.REVOKED.name -> {
                return LicenseValidationResult(
                    isValid = false,
                    status = LicenseStatus.REVOKED,
                    licenseId = record.license_id,
                    errorMessage = "License has been revoked."
                )
            }
            LicenseStatus.SUSPENDED.name -> {
                return LicenseValidationResult(
                    isValid = false,
                    status = LicenseStatus.SUSPENDED,
                    licenseId = record.license_id,
                    errorMessage = "License has been suspended."
                )
            }
            LicenseStatus.EXPIRED.name -> {
                return LicenseValidationResult(
                    isValid = false,
                    status = LicenseStatus.EXPIRED,
                    licenseId = record.license_id,
                    errorMessage = "License key has expired."
                )
            }
        }

        // Expiration timestamp check
        if (record.expires_at != null && record.expires_at < System.currentTimeMillis()) {
            return LicenseValidationResult(
                isValid = false,
                status = LicenseStatus.EXPIRED,
                licenseId = record.license_id,
                errorMessage = "License key has expired."
            )
        }

        // Device limit and management (up to 2 devices)
        val deviceId = customDeviceId ?: LicenseSecurity.getUniqueDeviceId(context)
        val registeredDevices = if (record.device_ids.isBlank()) {
            mutableListOf()
        } else {
            record.device_ids.split(",").filter { it.isNotBlank() }.toMutableList()
        }

        if (registeredDevices.contains(deviceId)) {
            // Re-activation on an already registered device
        } else {
            // New device registration attempt
            if (registeredDevices.size >= record.max_devices) {
                return LicenseValidationResult(
                    isValid = false,
                    status = LicenseStatus.ACTIVATION_LIMIT_REACHED,
                    licenseId = record.license_id,
                    activatedDevices = registeredDevices.size,
                    maxDevices = record.max_devices,
                    errorMessage = "Activation limit reached (maximum ${record.max_devices} devices allowed)."
                )
            }
            registeredDevices.add(deviceId)
        }

        val now = System.currentTimeMillis()
        val updatedRecord = record.copy(
            status = LicenseStatus.ACTIVE.name,
            activated_at = record.activated_at ?: now,
            activated_devices = registeredDevices.size,
            device_ids = registeredDevices.joinToString(","),
            last_seen = now
        )
        repository.saveLicenseRecord(updatedRecord)

        // Generate offline verification token
        val token = LicenseSecurity.createLocalActivationToken(
            licenseId = record.license_id,
            keyId = record.license_key_id,
            deviceId = deviceId,
            timestamp = updatedRecord.activated_at ?: now
        )

        val masked = LicenseSecurity.maskKey(normalized)

        // Save active license entity
        val activeLicense = LicenseEntity(
            id = 1,
            licenseId = record.license_id,
            maskedKey = masked,
            licenseKey = masked, // Never store complete key
            status = LicenseStatus.ACTIVE.name,
            activatedAt = updatedRecord.activated_at ?: now,
            expiresAt = record.expires_at ?: Long.MAX_VALUE,
            deviceId = deviceId,
            activationToken = token
        )
        repository.saveLicense(activeLicense)

        return LicenseValidationResult(
            isValid = true,
            status = LicenseStatus.ACTIVE,
            licenseId = record.license_id,
            maskedKey = masked,
            expiresAt = record.expires_at ?: Long.MAX_VALUE,
            tier = record.license_type,
            activatedDevices = registeredDevices.size,
            maxDevices = record.max_devices
        )
    }

    /**
     * Checks if the currently active license is in a valid state.
     */
    fun isLicenseActive(license: LicenseEntity?): Boolean {
        if (license == null) return false
        if (license.status != LicenseStatus.ACTIVE.name) return false
        if (System.currentTimeMillis() > license.expiresAt) return false
        return true
    }

    /**
     * Helper for resolving device identifier.
     */
    fun getDeviceId(context: Context): String {
        return LicenseSecurity.getUniqueDeviceId(context)
    }

    /**
     * Compatibility bridge for synchronous / legacy validation calls.
     */
    fun validateKey(key: String, context: Context? = null): LicenseValidationResult {
        val normalized = LicenseSecurity.normalizeKey(key)
        if (normalized.isBlank()) {
            return LicenseValidationResult(isValid = false, status = LicenseStatus.INVALID, errorMessage = "Invalid license key")
        }

        // Check if candidate matches any predefined key ID
        val keyId = LicenseSecurity.computeKeyId(normalized)
        val catalogItem = PredefinedLicenses.CATALOG.find { it.licenseKeyId == keyId }

        if (catalogItem != null) {
            val payload = catalogItem.payload()
            val validSig = LicenseSecurity.verifySignature(payload, catalogItem.signature)
            return if (validSig) {
                LicenseValidationResult(
                    isValid = true,
                    status = LicenseStatus.ACTIVE,
                    licenseId = catalogItem.licenseId,
                    maskedKey = LicenseSecurity.maskKey(normalized),
                    expiresAt = catalogItem.expiresAt,
                    tier = catalogItem.licenseType
                )
            } else {
                LicenseValidationResult(isValid = false, status = LicenseStatus.INVALID, errorMessage = "Invalid license signature")
            }
        }

        // Debug testing fallback
        if (BuildConfig.DEBUG && (normalized == "DEBUG-TEST-KEY" || normalized == "INV-PRO8-2028-9842-7B3A")) {
            return LicenseValidationResult(
                isValid = true,
                status = LicenseStatus.ACTIVE,
                licenseId = "LIC-DEBUG",
                maskedKey = "H40-PRO-••••-••••-TEST",
                tier = "PRO"
            )
        }

        return LicenseValidationResult(
            isValid = false,
            status = LicenseStatus.INVALID,
            errorMessage = "Invalid license key"
        )
    }

    // --- Admin Management APIs ---

    suspend fun revokeLicense(licenseId: String, repository: InventoryRepository): Boolean {
        val record = repository.getLicenseRecordById(licenseId) ?: return false
        val updated = record.copy(
            status = LicenseStatus.REVOKED.name,
            revoked_at = System.currentTimeMillis()
        )
        repository.saveLicenseRecord(updated)

        // If active license is this one, update active entity
        val active = repository.getLicenseDirect()
        if (active != null && active.licenseId == licenseId) {
            repository.saveLicense(active.copy(status = LicenseStatus.REVOKED.name))
        }
        return true
    }

    suspend fun suspendLicense(licenseId: String, repository: InventoryRepository): Boolean {
        val record = repository.getLicenseRecordById(licenseId) ?: return false
        val updated = record.copy(status = LicenseStatus.SUSPENDED.name)
        repository.saveLicenseRecord(updated)

        val active = repository.getLicenseDirect()
        if (active != null && active.licenseId == licenseId) {
            repository.saveLicense(active.copy(status = LicenseStatus.SUSPENDED.name))
        }
        return true
    }

    suspend fun reactivateLicense(licenseId: String, repository: InventoryRepository): Boolean {
        val record = repository.getLicenseRecordById(licenseId) ?: return false
        val updated = record.copy(status = LicenseStatus.ACTIVE.name, revoked_at = null)
        repository.saveLicenseRecord(updated)

        val active = repository.getLicenseDirect()
        if (active != null && active.licenseId == licenseId) {
            repository.saveLicense(active.copy(status = LicenseStatus.ACTIVE.name))
        }
        return true
    }

    suspend fun setDeviceLimit(licenseId: String, maxDevices: Int, repository: InventoryRepository): Boolean {
        val record = repository.getLicenseRecordById(licenseId) ?: return false
        val updated = record.copy(max_devices = maxDevices)
        repository.saveLicenseRecord(updated)
        return true
    }

    suspend fun setExpiration(licenseId: String, expiresAt: Long?, repository: InventoryRepository): Boolean {
        val record = repository.getLicenseRecordById(licenseId) ?: return false
        val updated = record.copy(expires_at = expiresAt)
        repository.saveLicenseRecord(updated)

        val active = repository.getLicenseDirect()
        if (active != null && active.licenseId == licenseId) {
            repository.saveLicense(active.copy(expiresAt = expiresAt ?: Long.MAX_VALUE))
        }
        return true
    }

    suspend fun resetDevices(licenseId: String, repository: InventoryRepository): Boolean {
        val record = repository.getLicenseRecordById(licenseId) ?: return false
        val updated = record.copy(
            device_ids = "",
            activated_devices = 0,
            status = LicenseStatus.AVAILABLE.name
        )
        repository.saveLicenseRecord(updated)

        val active = repository.getLicenseDirect()
        if (active != null && active.licenseId == licenseId) {
            repository.clearActiveLicense()
        }
        return true
    }
}
