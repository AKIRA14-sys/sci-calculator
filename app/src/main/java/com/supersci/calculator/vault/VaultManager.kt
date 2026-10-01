package com.supersci.calculator.vault

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class VaultManager(private val context: Context) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "SuperSciVaultMasterKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    init {
        getOrCreateMasterKey()
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val parameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }

    fun encryptFile(inputStream: InputStream, destinationFile: File) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = getOrCreateMasterKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        destinationFile.outputStream().use { output ->
            output.write(iv) // Write 12-byte IV first
            val cipherStream = javax.crypto.CipherOutputStream(output, cipher)
            inputStream.copyTo(cipherStream)
            cipherStream.close()
        }
    }

    fun decryptFileToStream(encryptedFile: File, outputStream: OutputStream) {
        encryptedFile.inputStream().use { input ->
            val iv = ByteArray(GCM_IV_LENGTH)
            val readIv = input.read(iv)
            if (readIv != GCM_IV_LENGTH) throw IllegalArgumentException("Invalid encrypted file IV format")

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getOrCreateMasterKey()
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val cipherStream = javax.crypto.CipherInputStream(input, cipher)
            cipherStream.copyTo(outputStream)
        }
    }

    fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getVaultDir(): File {
        val dir = File(context.filesDir, "vault_storage")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
