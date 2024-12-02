package com.example.passoff

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

class EncryptionUtils {

    companion object {
        private const val ALGORITHM = "AES"

        fun encrypt(message: String, key: String): String {
            val secretKeySpec = SecretKeySpec(key.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val encryptedBytes = cipher.doFinal(message.toByteArray())
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        }

        fun decrypt(encryptedMessage: String, key: String): String {
            val secretKeySpec = SecretKeySpec(key.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            val decodedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            return String(decryptedBytes)
        }
    }
}