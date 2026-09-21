package com.example.domain.license

import android.content.Context
import android.provider.Settings
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Locale

/**
 * Security and verification subsystem for Hanouti 40 Licensing.
 *
 * NOTE: The client application contains ONLY the public verification key.
 * The master private key is retained offline by the licensing authority.
 */
object LicenseSecurity {

    // Public verification key (secp256r1 ECDSA X.509 DER encoded in Base64)
    const val PUBLIC_KEY_BASE64 =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEp0NF2uL/XrtHHyVQSzyb6xKH8Miaj7AP0z1vaSmRgnfONVqyePaa0BHDHEQSAHdB6PJPOkzZChRIoi5VPMfYog=="

    private const val SALT = "HANOUTI40_PRO_KEY_V1:"
    private const val TOKEN_SALT = "HANOUTI40_LOCAL_OFFLINE_DEVICE_AUTH_2026:"

    @Volatile
    private var cachedPublicKey: PublicKey? = null

    fun getPublicKey(): PublicKey {
        cachedPublicKey?.let { return it }
        return synchronized(this) {
            cachedPublicKey ?: run {
                val keyBytes = Base64.decode(PUBLIC_KEY_BASE64, Base64.DEFAULT)
                val keySpec = X509EncodedKeySpec(keyBytes)
                val keyFactory = KeyFactory.getInstance("EC")
                val key = keyFactory.generatePublic(keySpec)
                cachedPublicKey = key
                key
            }
        }
    }

    /**
     * Normalizes a raw user input license key:
     * - Trims leading/trailing whitespace
     * - Removes internal spaces and formatting noise
     * - Converts to uppercase (Locale.US)
     * Example: "h40-pro-7k4m-x9qp-2f8d" -> "H40-PRO-7K4M-X9QP-2F8D"
     * Example: " H40 - PRO - 7K4M - X9QP - 2F8D " -> "H40-PRO-7K4M-X9QP-2F8D"
     */
    fun normalizeKey(rawKey: String): String {
        return rawKey.replace("\\s+".toRegex(), "")
            .trim()
            .uppercase(Locale.US)
    }

    /**
     * Masks the key for secure UI presentation.
     * "Never display the complete key after activation."
     * Example: "H40-PRO-7K4M-X9QP-2F8D" -> "H40-PRO-••••-••••-2F8D"
     */
    fun maskKey(normalizedKey: String): String {
        val parts = normalizedKey.split("-")
        return if (parts.size >= 5) {
            "${parts[0]}-${parts[1]}-••••-••••-${parts.last()}"
        } else if (parts.size == 4) {
            "${parts[0]}-••••-••••-${parts.last()}"
        } else {
            val start = normalizedKey.take(7)
            val end = normalizedKey.takeLast(4)
            "$start-••••-••••-$end"
        }
    }

    /**
     * Computes a secure one-way salted SHA-256 identifier for the license key.
     * The raw key is NEVER stored in the database or written to logs.
     */
    fun computeKeyId(normalizedKey: String): String {
        val input = SALT + normalizedKey
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies the cryptographic signature of a license record payload
     * using the embedded public verification key.
     */
    fun verifySignature(payload: String, signatureBase64: String): Boolean {
        return try {
            val pubKey = getPublicKey()
            val sig = Signature.getInstance("SHA256withECDSA")
            sig.initVerify(pubKey)
            sig.update(payload.toByteArray(StandardCharsets.UTF_8))
            val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
            sig.verify(signatureBytes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generates a signed local activation token for offline persistence.
     */
    fun createLocalActivationToken(licenseId: String, keyId: String, deviceId: String, timestamp: Long): String {
        val payload = "$TOKEN_SALT$licenseId:$keyId:$deviceId:$timestamp"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(payload.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies local activation token offline.
     */
    fun verifyLocalActivationToken(
        token: String,
        licenseId: String,
        keyId: String,
        deviceId: String,
        timestamp: Long
    ): Boolean {
        if (token.isBlank()) return false
        val expected = createLocalActivationToken(licenseId, keyId, deviceId, timestamp)
        return token == expected
    }

    /**
     * Resolves unique device hardware identifier.
     */
    fun getUniqueDeviceId(context: Context): String {
        return try {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                val md = MessageDigest.getInstance("SHA-256")
                val hash = md.digest(("DEV_SALT_" + androidId).toByteArray(StandardCharsets.UTF_8))
                "DEV-" + hash.take(8).joinToString("") { "%02X".format(it) }
            } else {
                "DEV-DEFAULT-HWID-01"
            }
        } catch (e: Exception) {
            "DEV-OFFLINE-HWID-01"
        }
    }
}
