package com.example.data.util

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale

object SecurityUtils {

    private const val PREMIUM_REDEEM_SALT = "AuraPulse_Redeem_v1_Salt:"
    private const val PREMIUM_REDEEM_SHA256_HEX =
        "1d4d5fc96d88d64f7a6ee03c0300f52f2447f91ea06e2a2973f9056c3c49b1b7"

    private const val VAULT_HASH_ITERATIONS = 10_000
    private const val DEFAULT_VAULT_SALT = "AuraPulse_DefaultVault_v1"

    private val secureRandom = SecureRandom()

    fun verifyPremiumRedeemCode(input: String): Boolean {
        val normalized = input.trim()
        if (normalized.isEmpty()) return false
        val actualDigest = sha256Bytes(PREMIUM_REDEEM_SALT + normalized)
        val expectedDigest = hexToBytes(PREMIUM_REDEEM_SHA256_HEX)
        return MessageDigest.isEqual(actualDigest, expectedDigest)
    }

    fun generateSaltHex(numBytes: Int = 16): String {
        val bytes = ByteArray(numBytes.coerceIn(8, 32))
        secureRandom.nextBytes(bytes)
        return bytesToHex(bytes)
    }

    fun hashVaultPin(pin: String, saltHex: String): String {
        var current = "$saltHex:$pin".toByteArray(StandardCharsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        repeat(VAULT_HASH_ITERATIONS) {
            md.reset()
            current = md.digest(current)
        }
        return bytesToHex(current)
    }

    fun verifyVaultPinHash(inputPin: String, saltHex: String, expectedDigestHex: String): Boolean {
        if (inputPin.isEmpty() || saltHex.isEmpty() || expectedDigestHex.isEmpty()) return false
        val actual = hexToBytes(hashVaultPin(inputPin, saltHex))
        val expected = hexToBytes(expectedDigestHex)
        if (actual.isEmpty() || expected.isEmpty()) return false
        return MessageDigest.isEqual(actual, expected)
    }

    fun hashDefaultVaultPin(pin: String): String = hashVaultPin(pin, DEFAULT_VAULT_SALT)

    fun defaultVaultSalt(): String = DEFAULT_VAULT_SALT

    fun sha256Hex(input: String): String {
        return bytesToHex(sha256Bytes(input))
    }

    private fun sha256Bytes(input: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(input.toByteArray(StandardCharsets.UTF_8))
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(String.format(Locale.US, "%02x", b))
        }
        return sb.toString()
    }

    private fun hexToBytes(hex: String): ByteArray {
        if (hex.length % 2 != 0) return ByteArray(0)
        return try {
            ByteArray(hex.length / 2) { index ->
                val hi = Character.digit(hex[index * 2], 16)
                val lo = Character.digit(hex[index * 2 + 1], 16)
                require(hi >= 0 && lo >= 0)
                ((hi shl 4) or lo).toByte()
            }
        } catch (_: Exception) {
            ByteArray(0)
        }
    }
}
