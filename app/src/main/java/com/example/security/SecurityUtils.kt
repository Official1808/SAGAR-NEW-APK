package com.example.security

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {

    private val secureRandom = SecureRandom()
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    /**
     * Normalizes username for case-insensitive comparison.
     * "SAGAR", "Sagar", "sagar" -> "sagar"
     */
    fun normalizeUsername(username: String): String {
        return username.trim().lowercase(Locale.ROOT)
    }

    /**
     * Generates a cryptographically secure random salt in hex string format.
     */
    fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)
        return bytesToHex(salt)
    }

    /**
     * Hashes password using PBKDF2WithHmacSHA256 (or SHA-256 fallback) with salt.
     * Passwords remain strictly case-sensitive.
     */
    fun hashPassword(password: String, saltHex: String): String {
        return try {
            val salt = hexToBytes(saltHex)
            val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            bytesToHex(hash)
        } catch (e: Exception) {
            // Fallback salted SHA-256 with multiple rounds
            val md = MessageDigest.getInstance("SHA-256")
            md.update(hexToBytes(saltHex))
            var digest = md.digest(password.toByteArray(Charsets.UTF_8))
            for (i in 0 until 1000) {
                md.reset()
                digest = md.digest(digest)
            }
            bytesToHex(digest)
        }
    }

    /**
     * Verifies if input password matches the stored hash using constant-time comparison.
     */
    fun verifyPassword(password: String, saltHex: String, expectedHashHex: String): Boolean {
        val computedHashHex = hashPassword(password, saltHex)
        return slowEquals(computedHashHex.toByteArray(Charsets.UTF_8), expectedHashHex.toByteArray(Charsets.UTF_8))
    }

    /**
     * Generates a secure random URL-safe token (e.g. for signed email approval/deny links or session tokens).
     */
    fun generateSecureToken(byteLength: Int = 32): String {
        val bytes = ByteArray(byteLength)
        secureRandom.nextBytes(bytes)
        return bytesToHex(bytes)
    }

    /**
     * Constant-time comparison to prevent timing attacks.
     */
    private fun slowEquals(a: ByteArray, b: ByteArray): Boolean {
        var diff = a.size xor b.size
        val minLen = Math.min(a.size, b.size)
        for (i in 0 until minLen) {
            diff = diff or (a[i].toInt() xor b[i].toInt())
        }
        return diff == 0
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        val hexArray = "0123456789abcdef".toCharArray()
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v ushr 4]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
