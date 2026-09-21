package com.example.domain.license

enum class LicenseStatus(val displayName: String) {
    AVAILABLE("Available"),
    ACTIVE("Active"),
    INVALID("Invalid"),
    REVOKED("Revoked"),
    SUSPENDED("Suspended"),
    ACTIVATION_LIMIT_REACHED("Limit Reached"),
    EXPIRED("Expired")
}

data class PredefinedLicenseRecord(
    val licenseId: String,
    val licenseKeyId: String,
    val licenseType: String = "PRO",
    val maxDevices: Int = 2,
    val createdAt: Long = 1735689600000L,
    val expiresAt: Long = Long.MAX_VALUE,
    val signature: String
) {
    fun payload(): String {
        return "$licenseId|$licenseKeyId|$licenseType|$maxDevices|$createdAt|$expiresAt"
    }
}

data class LicenseValidationResult(
    val isValid: Boolean,
    val status: LicenseStatus,
    val licenseId: String? = null,
    val maskedKey: String? = null,
    val expiresAt: Long = Long.MAX_VALUE,
    val tier: String = "PRO",
    val errorMessage: String? = null,
    val activatedDevices: Int = 0,
    val maxDevices: Int = 2
)
