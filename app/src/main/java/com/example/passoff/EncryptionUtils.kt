import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

class EncryptionUtils {

    companion object {

        fun deriveKeyFromMatchCode(matchCode: String): ByteArray {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(matchCode.toByteArray(Charsets.UTF_8))
        }

        fun encrypt(message: String, key: ByteArray): String {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKeySpec = SecretKeySpec(key, "AES")
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)
            val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
            val ivAndEncryptedMessage = iv + encryptedBytes
            return Base64.encodeToString(ivAndEncryptedMessage, Base64.DEFAULT)
        }

        fun decrypt(encryptedMessage: String, key: ByteArray): String {
            val ivAndEncryptedMessage = Base64.decode(encryptedMessage, Base64.DEFAULT)
            val iv = ivAndEncryptedMessage.copyOfRange(0, 16)
            val encryptedBytes = ivAndEncryptedMessage.copyOfRange(16, ivAndEncryptedMessage.size)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKeySpec = SecretKeySpec(key, "AES")
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        }
    }
}
