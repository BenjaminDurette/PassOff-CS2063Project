import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import java.security.MessageDigest
import javax.crypto.KeyGenerator
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory

class EncryptionUtils {

    companion object {
        private const val ALGORITHM = "AES"
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val SALT_LENGTH = 16 // Length for salt (in bytes)
        private const val ITERATIONS = 10000

        // Generic function to encrypt a message using a key
        fun encrypt(message: String, key: ByteArray): String {
            // Example encryption logic using AES
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKeySpec = SecretKeySpec(key, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        }

        fun decrypt(encryptedMessage: String, key: ByteArray): String {
            // Example decryption logic using AES
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKeySpec = SecretKeySpec(key, "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            val decodedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        }

        // Function to derive an AES key from a password using PBKDF2
        fun deriveKeyFromPassword(masterPassword: String): SecretKey {
            val salt = ByteArray(SALT_LENGTH)
            // Use a simple salt here, ideally, you should use a more secure random salt storage method
            val random = java.security.SecureRandom()
            random.nextBytes(salt)

            val spec = PBEKeySpec(masterPassword.toCharArray(), salt, ITERATIONS, 256)
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val key = factory.generateSecret(spec)
            return SecretKeySpec(key.encoded, ALGORITHM)
        }

        // Function to generate a random salt (for use with password items or other cases)
        fun generateSalt(): ByteArray {
            val salt = ByteArray(SALT_LENGTH)
            val random = java.security.SecureRandom()
            random.nextBytes(salt)
            return salt
        }

        fun deriveKeyFromMatchCode(matchCode: String): ByteArray {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(matchCode.toByteArray(Charsets.UTF_8))
        }
    }
}
