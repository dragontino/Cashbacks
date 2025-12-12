package com.cashbacks.core.database.room.encryption

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class PassphraseEncryptionHelper {
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    init {
        generateKeystoreKeyIfNeeded()
    }

    fun encryptPass(message: ByteArray): EncryptionData {
        val key = getSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }

        val encryptedPass = cipher.doFinal(message)
        return EncryptionData(
            encryptedBytes = encryptedPass,
            iv = cipher.iv
        )
    }

    fun decryptPass(encryptionData: EncryptionData): ByteArray {
        val secretKey = getSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            val spec = GCMParameterSpec(128, encryptionData.iv)
            init(Cipher.DECRYPT_MODE, secretKey, spec)
        }
        return cipher.doFinal(encryptionData.encryptedBytes)
    }


    private fun generateKeystoreKeyIfNeeded() {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(ALGORITHM, ANDROID_KEYSTORE)
            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(BLOCK_MODE)
                .setEncryptionPaddings(PADDING)
                .build()

            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey =
        (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey



    private companion object {
        private const val KEYSTORE_ALIAS = "ru.dragontino.cashbacks.android.security.key"

        private const val ANDROID_KEYSTORE = "AndroidKeyStore"

        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}