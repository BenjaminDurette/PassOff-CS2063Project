package com.example.passoff

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

object EncryptionUtils {
    private const val SALT = "eldenRing" // Use a fixed salt value

    fun deriveKeyFromString(password: String): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec = PBEKeySpec(password.toCharArray(), SALT.toByteArray(), 10000, 256)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    fun encrypt(data: String, key: SecretKeySpec): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    fun decrypt(data: String, key: SecretKeySpec): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decrypted = cipher.doFinal(Base64.decode(data, Base64.DEFAULT))
        return String(decrypted)
    }
}