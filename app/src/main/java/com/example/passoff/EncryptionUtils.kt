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
        fun encrypt(message: String, key: String): String {
            val secretKey = SecretKeySpec(key.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedBytes = cipher.doFinal(message.toByteArray())
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        }

        // Generic function to decrypt a message using a key
        fun decrypt(encryptedMessage: String, key: String): String {
            val secretKey = SecretKeySpec(key.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decodedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            return String(decryptedBytes)
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
    }
}
